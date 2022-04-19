@file:Suppress("MagicNumber")

import edu.illinois.cs.cs124.playground.Result
import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.server.Status
import edu.illinois.cs.cs124.playground.server.listPlaygroundImages
import edu.illinois.cs.cs124.playground.server.playground
import edu.illinois.cs.cs124.playground.server.toJson
import edu.illinois.cs.cs124.playground.server.versionString
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("LargeClass")
class TestMain : StringSpec() {
    init {
        "should GET status" {
            testApplication {
                application {
                    playground()
                }
                client.get("/") {
                    header("content-type", "application/json")
                }.let { response ->
                    response.status shouldBe HttpStatusCode.OK
                    statusFrom(response.bodyAsText()).apply {
                        version shouldBe versionString
                    }
                }
            }
        }
        "should POST python job" {
            val submission =
                Submission(
                    "cs124/playground-runner-python",
                    listOf(Submission.FakeFile("main.py", """print("Hello, Python!")""")),
                    4000L
                ).toJson()

            testApplication {
                application {
                    playground()
                }
                client.post("/") {
                    header("content-type", "application/json")
                    setBody(submission)
                }.let { response ->
                    response.status shouldBe HttpStatusCode.OK
                    resultFrom(response.bodyAsText()).apply {
                        timedOut shouldBe false
                        output shouldBe "Hello, Python!"
                    }
                }
            }
        }
        "test list containers" {
            val username = System.getenv("DOCKER_USER")
            val password = System.getenv("DOCKER_PASSWORD")
            if (username?.isNotEmpty() == true && password?.isNotEmpty() == true) {
                listPlaygroundImages(username, password).results.filter { it.name.startsWith("playground-runner-") } shouldHaveSize 11
            }
        }
    }
}

fun statusFrom(response: String?): Status {
    check(response != null) { "can't deserialize null string" }
    return Json.decodeFromString(response)
}

fun resultFrom(response: String?): Result {
    check(response != null) { "can't deserialize null string" }
    return Json.decodeFromString(response)
}
