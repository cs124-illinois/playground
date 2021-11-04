@file:Suppress("MagicNumber")

import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.server.listPlaygroundImages
import edu.illinois.cs.cs124.playground.server.playground
import edu.illinois.cs.cs124.playground.server.resultFrom
import edu.illinois.cs.cs124.playground.server.statusFrom
import edu.illinois.cs.cs124.playground.server.toJson
import edu.illinois.cs.cs124.playground.server.versionString
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication

@Suppress("LargeClass")
class TestMain : StringSpec() {
    init {
        "should GET status" {
            withTestApplication(Application::playground) {
                handleRequest(HttpMethod.Get, "/") {
                    addHeader("content-type", "application/json")
                }.apply {
                    response.shouldHaveStatus(HttpStatusCode.OK.value)
                    statusFrom(response.content).apply {
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
            withTestApplication(Application::playground) {
                handleRequest(HttpMethod.Post, "/") {
                    addHeader("content-type", "application/json")
                    setBody(submission)
                }.apply {
                    response.shouldHaveStatus(HttpStatusCode.OK.value)
                    resultFrom(response.content).apply {
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
               listPlaygroundImages(username, password)
            }
        }
    }
}
