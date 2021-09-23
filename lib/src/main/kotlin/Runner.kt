import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zeroturnaround.process.ProcessUtil
import org.zeroturnaround.process.Processes
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeBytes

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

data class RunResult(
    val started: Instant,
    val ended: Instant,
    val outputLines: List<OutputLine>,
    val timedOut: Boolean,
    val exitValue: Int
) {
    val output: String = outputLines.output()
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun run(image: String, timeout: Int = 8000, filesystem: Map<String, String> = mapOf()): RunResult =
    withContext(Dispatchers.IO) {
        val directory = createTempDirectory("playground")
        filesystem.entries.forEach { (path, contents) ->
            directory.resolve(path).writeBytes(contents.toByteArray())
        }
        try {
            val dockerName = UUID.randomUUID().toString()
            val command = "docker run --name=$dockerName -v ${directory.absolutePathString()}:/playground $image"

            @Suppress("SpreadOperator")
            val processBuilder =
                ProcessBuilder(*listOf("/bin/sh", "-c", command).toTypedArray()).directory(directory.toFile())

            val started = Instant.now()
            val process = processBuilder.start()
            val stdoutLines = StreamGobbler(OutputLine.Console.STDOUT, process.inputStream)
            val stderrLines = StreamGobbler(OutputLine.Console.STDERR, process.errorStream)
            val stderrThread = Thread(stdoutLines)
            val stdoutThread = Thread(stderrLines)
            stderrThread.start()
            stdoutThread.start()

            val timedOut = !process.waitFor(timeout.toLong(), TimeUnit.MILLISECONDS)
            if (timedOut) {
                val dockerStopCommand = """docker kill ${"$"}(docker ps -q --filter="name=$dockerName")"""
                Runtime.getRuntime().exec(listOf("/bin/sh", "-c", dockerStopCommand).toTypedArray()).waitFor()
                ProcessUtil.destroyForcefullyAndWait(Processes.newStandardProcess(process))
            }
            val ended = Instant.now()
            val exitValue = process.exitValue()

            stderrThread.join()
            stdoutThread.join()

            return@withContext RunResult(
                started,
                ended,
                (stdoutLines.outputLines + stderrLines.outputLines).sortedBy { it.timestamp },
                timedOut,
                exitValue
            )
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
