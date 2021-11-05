package edu.illinois.cs.cs124.playground

import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import org.zeroturnaround.process.ProcessUtil
import org.zeroturnaround.process.Processes
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeBytes

private val logger = KotlinLogging.logger {}

@JsonClass(generateAdapter = true)
data class OutputLine(
    val console: Console,
    val timestamp: Instant,
    val line: String
) {
    enum class Console(@Suppress("unused") val fd: Int) { STDOUT(1), STDERR(2) }
}

fun List<OutputLine>.output() = joinToString("\n") { it.line }

class StreamGobbler(
    private val console: OutputLine.Console,
    private val inputStream: InputStream,
) : Runnable {
    val outputLines: MutableList<OutputLine> = mutableListOf()
    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines().forEach { line ->
            if (line == null) {
                return@forEach
            }
            outputLines.add(OutputLine(console, Instant.now(), line))
        }
    }
}

@JsonClass(generateAdapter = true)
data class Submission(val image: String, val filesystem: List<FakeFile> = listOf(), val timeout: Long) {
    @JsonClass(generateAdapter = true)
    data class FakeFile(val path: String, val contents: String)
}

private val loadedImages = mutableSetOf<String>()

@Suppress("unused")
fun String.load(pullTimeout: Long = 60000L, echo: Boolean = false): Unit = CoroutineScope(Dispatchers.IO).run {
    if (loadedImages.contains(this@load)) {
        logger.trace { "Skipping load: Already loaded ${this@load}" }
        return@load
    }
    logger.debug { "Loading image ${this@load}" }
    ProcessBuilder(*listOf("/bin/sh", "-c", "docker pull ${this@load}").toTypedArray())
        .also {
            if (echo) {
                it.inheritIO()
            }
        }
        .start().also { process ->
            process.waitFor(pullTimeout, TimeUnit.MILLISECONDS).also {
                check(it) { "Timed out pulling container: ${this@load}" }
            }
            check(process.exitValue() == 0) {
                logger.warn { "Loading image ${this@load} failed: ${process.exitValue()}" }
                "Failed to pull container: ${this@load}"
            }
            logger.debug { "Loading image ${this@load} succeeded" }
            loadedImages += this@load
        }
}

@Suppress("unused")
fun String.inspect(): Boolean = CoroutineScope(Dispatchers.IO).run {
    if (loadedImages.contains(this@inspect)) {
        logger.trace { "Skipping inspect: Already loaded ${this@inspect}" }
        return true
    }
    logger.debug { "Inspecting image ${this@inspect}" }
    return ProcessBuilder(*listOf("/bin/sh", "-c", "docker inspect ${this@inspect} > /dev/null").toTypedArray())
        .start().let { process ->
            process.waitFor(1000, TimeUnit.MILLISECONDS).also {
                check(it) {
                    logger.warn { "Inspecting image ${this@inspect} failed: ${process.exitValue()}" }
                    "Timed out inspecting container: ${this@inspect}"
                }
            }
            logger.debug { "Inspecting image ${this@inspect} succeeded: ${process.exitValue() == 0}" }
            process.exitValue() == 0
        }
}

@JsonClass(generateAdapter = true)
data class Result(
    val outputLines: List<OutputLine>,
    val timeout: Long,
    val timedOut: Boolean,
    val exitValue: Int,
    val timings: Timings
) {
    val output: String = outputLines.output()

    @JsonClass(generateAdapter = true)
    data class Timings(
        val started: Instant,
        val tempCreated: Instant,
        val imagePulled: Instant,
        val containerStarted: Instant,
        val completed: Instant?
    )
}

fun Submission.run(tempRoot: String? = null, pullTimeout: Long = 60000L): Result =
    CoroutineScope(Dispatchers.IO).runCatching {

        val started = Instant.now()
        val directory = if (tempRoot == null) {
            createTempDirectory("playground")
        } else {
            File(tempRoot).mkdirs()
            createTempDirectory(Path(tempRoot), "playground")
        }
        filesystem.forEach { (path, contents) ->
            directory.resolve(path).writeBytes(contents.toByteArray())
        }
        val tempCreated = Instant.now()
        try {
            if (!image.inspect()) {
                logger.debug { "Run requires loading $image" }
                image.load(pullTimeout)
                logger.debug { "Completed image pull" }
            }
            val imagePulled = Instant.now()

            val dockerName = UUID.randomUUID().toString()
            val command =
                "docker run --init --rm --network=none --name=$dockerName -v ${directory.absolutePathString()}:/playground $image"

            logger.trace { "Running $command with timeout $timeout" }
            @Suppress("SpreadOperator")
            val processBuilder =
                ProcessBuilder(*listOf("/bin/sh", "-c", command).toTypedArray()).directory(directory.toFile())

            val containerStarted = Instant.now()
            val process = processBuilder.start()
            val stdoutLines = StreamGobbler(OutputLine.Console.STDOUT, process.inputStream)
            val stderrLines = StreamGobbler(OutputLine.Console.STDERR, process.errorStream)
            val stderrThread = Thread(stdoutLines)
            val stdoutThread = Thread(stderrLines)
            stderrThread.start()
            stdoutThread.start()

            val timedOut = !process.waitFor(timeout, TimeUnit.MILLISECONDS)
            if (timedOut) {
                val dockerStopCommand = """docker kill ${"$"}(docker ps -q --filter="name=$dockerName")"""
                Runtime.getRuntime().exec(listOf("/bin/sh", "-c", dockerStopCommand).toTypedArray()).waitFor()
                ProcessUtil.destroyForcefullyAndWait(Processes.newStandardProcess(process))
            }
            val completed = if (timedOut) {
                null
            } else {
                Instant.now()
            }
            val exitValue = process.exitValue()

            stderrThread.join()
            stdoutThread.join()

            return@runCatching Result(
                (stdoutLines.outputLines + stderrLines.outputLines).sortedBy { it.timestamp },
                timeout,
                timedOut,
                exitValue,
                Result.Timings(started, tempCreated, imagePulled, containerStarted, completed)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            directory.toFile().deleteRecursively()
        }
    }.getOrThrow()
