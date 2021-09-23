@file:Suppress("MagicNumber")

import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs125.questioner.server.playground
import edu.illinois.cs.cs125.questioner.server.resultFrom
import edu.illinois.cs.cs125.questioner.server.statusFrom
import edu.illinois.cs.cs125.questioner.server.toJson
import edu.illinois.cs.cs125.questioner.server.versionString
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
        "should POST helloworld submission" {
            val submission = Submission("cs124/helloworld").toJson()
            withTestApplication(Application::playground) {
                handleRequest(HttpMethod.Post, "/") {
                    addHeader("content-type", "application/json")
                    setBody(submission)
                }.apply {
                    response.shouldHaveStatus(HttpStatusCode.OK.value)
                    resultFrom(response.content).apply {
                        timedOut shouldBe false
                        output shouldBe "Hello, world!"
                    }
                }
            }
        }
        "should POST python job" {
            val submission =
                Submission("cs124/python", mapOf("main.py" to """print("Hello, Python!")""")).toJson()
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
    }
}
