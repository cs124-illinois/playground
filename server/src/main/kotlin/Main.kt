package edu.illinois.cs.cs124.playground.server

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import edu.illinois.cs.cs124.playground.Result
import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.load
import edu.illinois.cs.cs124.playground.run
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.time.Instant
import java.util.Properties
import java.util.concurrent.TimeUnit

private val serverStarted = Instant.now()
private val logger = KotlinLogging.logger {}

object DockerSpec : ConfigSpec() {
    val host by required<String>()
    val user by optional<String?>(null)
    val password by optional<String?>(null)
}

object TopLevel : ConfigSpec("") {
    val directory by optional<String?>(null)
    val preload by optional(true)
}

val configuration = Config {
    addSpec(TopLevel)
    addSpec(DockerSpec)
}.from.env()

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
            withContext(Dispatchers.IO) {
                try {
                    val result = call.receive<Submission>().run(tempRoot = configuration[TopLevel.directory])
                    call.respond(result)
                } catch (e: Exception) {
                    logger.error { e }
                    call.respond(HttpStatusCode.BadRequest)
                    return@withContext
                }
            }
        }
    }
}

fun dockerLogin(username: String, password: String) {
    logger.info("Logging in to Docker as $username...")
    @Suppress("SpreadOperator")
    ProcessBuilder(
        *listOf(
            "/bin/sh",
            "-c",
            "docker login --username $username --password-stdin"
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

private data class LoginRequest(val username: String, val password: String)
private data class TokenResponse(val token: String)
data class ListResponse(val count: Int, val results: List<Result>) {
    data class Result(val name: String)
}

suspend fun listPlaygroundImages(username: String, password: String): ListResponse {
    val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    val httpClient = HttpClient(CIO)

    val token = httpClient.post<HttpResponse>("https://hub.docker.com/v2/users/login/") {
        header("Content-Type", "application/json")
        body = mapper.writeValueAsString(LoginRequest(username, password))
    }.let { response ->
        mapper.readValue<TokenResponse>(response.receive<String>()).token
    }

    return httpClient.request<String>("https://hub.docker.com/v2/repositories/cs124") {
        header("Authorization", "JWT $token")
    }.let { response ->
        mapper.readValue(response)
    }
}

private val backgroundScope = CoroutineScope(Dispatchers.IO)

fun main() {
    logger.info("Waiting for Docker...")
    configuration[DockerSpec.host].waitFor(2375, 32000L)
    logger.info("Done")

    if (configuration[DockerSpec.user] != null && configuration[DockerSpec.user] != "") {
        check(configuration[DockerSpec.password] != null) { "Docker password required" }
        val username = configuration[DockerSpec.user]!!
        val password = configuration[DockerSpec.password]!!

        dockerLogin(username, password)

        if (configuration[TopLevel.preload]) {
            backgroundScope.launch {
                listPlaygroundImages(username, password).results.forEach { result ->
                    if (result.name.startsWith("playground-runner-")) {
                        "cs124/${result.name}".load()
                        logger.debug { "Loaded cs124/${result.name}" }
                    }
                }
            }
        }
    }

    logger.debug { status }
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

fun Submission.toJson(): String = moshi.adapter(Submission::class.java).toJson(this)

@Suppress("NestedBlockDepth")
fun String.waitFor(defaultPort: Int = -1, timeout: Long = 16000) {
    val parts = this.split(":")
    val (host, port) = if (parts.size == 2) {
        Pair(parts[0], parts[1].toInt())
    } else {
        Pair(this, defaultPort)
    }
    val started = Instant.now().toEpochMilli()
    while (Instant.now().toEpochMilli() - started < timeout) {
        try {
            Socket().use {
                it.soTimeout = timeout.toInt()
                it.connect(InetSocketAddress(host, port))
                if (it.isConnected) {
                    return@waitFor
                }
            }
        } catch (e: IOException) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }
    }
    throw ConnectException("couldn't connect to $host:$port")
}
