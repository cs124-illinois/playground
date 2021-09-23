package edu.illinois.cs.cs125.questioner.server

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import edu.illinois.cs.cs124.playground.Result
import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.run
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import java.time.Instant
import java.util.Properties

private val serverStarted = Instant.now()
private val logger = KotlinLogging.logger {}

object TopLevel : ConfigSpec("") {
    val directory by optional<String?>(null)
    val dockerUser by optional<String?>(null)
    val dockerPassword by optional<String?>(null)
}
val configuration = Config { addSpec(TopLevel) }.from.env()

class InstantAdapter {
    @FromJson
    fun instantFromJson(timestamp: String): Instant {
        return Instant.parse(timestamp)
    }

    @ToJson
    fun instantToJson(instant: Instant): String {
        return instant.toString()
    }
}

val moshi: Moshi = Moshi.Builder().apply {
    add(InstantAdapter())
}.build()

val versionString = run {
    @Suppress("TooGenericExceptionCaught")
    try {
        val versionFile = object {}::class.java.getResource("/edu.illinois.cs.cs124.playground.server.version")
        Properties().also { it.load(versionFile!!.openStream()) }["version"] as String
    } catch (e: Exception) {
        "unspecified"
    }
}

@JsonClass(generateAdapter = true)
data class Status(val started: Instant = serverStarted, val version: String = versionString)

val status = Status()

@Suppress("LongMethod")
fun Application.playground() {
    install(ContentNegotiation) {
        moshi {
            add(InstantAdapter())
        }
    }
    routing {
        get("/") {
            call.respond(status)
        }
        post("/") {
            val result = try {
                call.receive<Submission>().run(tempRoot = configuration[TopLevel.directory])
            } catch (e: Exception) {
                logger.error { e }
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            call.respond(result)
        }
    }
}

fun dockerLogin(user: String, password: String) {
    logger.info("Logging in to Docker as $user...")
    @Suppress("SpreadOperator")
    ProcessBuilder(
        *listOf(
            "/bin/sh",
            "-c",
            "docker login --username $user --password-stdin"
        ).toTypedArray()
    ).start().also { process ->
        process.outputStream.also {
            it.write(password.toByteArray())
            it.flush()
            it.close()
        }
        process.waitFor()
        check(process.exitValue() == 0) { "Login failed" }
    }
    logger.info("Done")
}

fun main() {
    logger.debug { status }
    if (configuration[TopLevel.dockerUser] != null) {
        check(configuration[TopLevel.dockerPassword] != null) { "Docker password required" }
        dockerLogin(configuration[TopLevel.dockerUser]!!, configuration[TopLevel.dockerPassword]!!)
    }
    embeddedServer(Netty, port = 8888, module = Application::playground).start(wait = true)
}

fun statusFrom(response: String?): Status {
    check(response != null) { "can't deserialize null string" }
    return moshi.adapter(Status::class.java).fromJson(response) ?: error("failed to deserialize status")
}

fun resultFrom(response: String?): Result {
    check(response != null) { "can't deserialize null string" }
    return moshi.adapter(Result::class.java).fromJson(response) ?: error("failed to deserialize result")
}

fun Submission.toJson() = moshi.adapter(Submission::class.java).toJson(this)
