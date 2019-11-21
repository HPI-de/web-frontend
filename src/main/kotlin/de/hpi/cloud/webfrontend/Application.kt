package de.hpi.cloud.webfrontend

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.headers
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.path
import io.ktor.request.receiveChannel
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.copyAndClose
import org.slf4j.event.Level
import java.net.URI

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = true) {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        if (testing) {
            anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
        }
    }

    install(CachingHeaders) {
        options { outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60),
                    expires = null as? GMTDate?
                )
                else -> null
            }
        }
    }

    install(DefaultHeaders) {
        // anonymize server
        header("Server", "httpd 3.0A")
    }

    install(HSTS) {
        includeSubDomains = true
    }

    // https://ktor.io/servers/features/https-redirect.html#testing
    if (!testing) {
        install(HttpsRedirect) {
            // The port to redirect to. By default 443, the default HTTPS port.
            sslPort = 443
            // 301 Moved Permanently, or 302 Found redirect.
            permanentRedirect = true
        }
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        post("/hpi.cloud.news.v1test.NewsService/{method}") {
            val client = HttpClient()
            val result =
                client.call(
                    URLBuilder(
                        host = "172.18.132.7",
                        port = 50061,
                        encodedPath = URI.create(call.request.uri).rawPath
                    ).build()
                ) {
                    method = HttpMethod.Post

                    // Forward headers
                    headers {
                        call.request.headers
                            .filterContentTypeLength()
                            .forEach { name, values -> appendAll(name, values) }
                    }

                    val receiveChannel = call.receiveChannel()
                    body = object : OutgoingContent.ReadChannelContent() {
                        override val contentType =
                            call.request.headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }

                        override fun readFrom() = receiveChannel
                    }
                }

            val proxiedHeaders = result.response.headers
            val contentType = proxiedHeaders[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]?.toLong()
            call.respond(object : OutgoingContent.WriteChannelContent() {
                override val contentLength: Long? = contentLength
                override val contentType: ContentType? = contentType
                override val headers: Headers = Headers.build {
                    appendAll(proxiedHeaders.filterContentTypeLength())
                }
                override val status: HttpStatusCode? = result.response.status
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    result.response.content.copyAndClose(channel)
                }
            })

//            // We create a GET request to the wikipedia domain and return the call (with the request and the unprocessed response).
//            val result = client.call("https://$wikipediaLang.wikipedia.org${call.request.uri}")
//
//            // Get the relevant headers of the client response.
//            val proxiedHeaders = result.response.headers
//            val location = proxiedHeaders[HttpHeaders.Location]
//            val contentType = proxiedHeaders[HttpHeaders.ContentType]
//            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]
//
//            // Extension method to process all the served HTML documents
//            fun String.stripWikipediaDomain() = this.replace(Regex("(https?:)?//\\w+\\.wikipedia\\.org"), "")
//
//            // Propagates location header, removing the wikipedia domain from it
//            if (location != null) {
//                call.response.header(HttpHeaders.Location, location.stripWikipediaDomain())
//            }
//
//            // Depending on the ContentType, we process the request one way or another.
//            when {
//                // In the case of HTML we download the whole content and process it as a string replacing
//                // wikipedia links.
//                contentType?.startsWith("text/html") == true -> {
//                    val text = result.response.readText()
//                    val filteredText = text.stripWikipediaDomain()
//                    call.respond(
//                        TextContent(
//                            filteredText,
//                            ContentType.Text.Html.withCharset(Charsets.UTF_8),
//                            result.response.status
//                        )
//                    )
//                }
//                else -> {
//                    // In the case of other content, we simply pipe it. We return a [OutgoingContent.WriteChannelContent]
//                    // propagating the contentLength, the contentType and other headers, and simply we copy
//                    // the ByteReadChannel from the HTTP client response, to the HTTP server ByteWriteChannel response.
//                    call.respond(object : OutgoingContent.WriteChannelContent() {
//                        override val contentLength: Long? = contentLength?.toLong()
//                        override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
//                        override val headers: Headers = Headers.build {
//                            appendAll(proxiedHeaders.filter { key, _ -> !key.equals(HttpHeaders.ContentType, ignoreCase = true) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true) })
//                        }
//                        override val status: HttpStatusCode? = result.response.status
//                        override suspend fun writeTo(channel: ByteWriteChannel) {
//                            result.response.content.copyAndClose(channel)
//                        }
//                    })
//                }
//            }
        }
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
        get("/ktor") {
            call.respondText(
                "<!DOCTYPE html><head><title>Home</title></head><body><h1>Hello Ktor!</h1><img src=\"/static/ktor_logo.svg\"/></body></html>",
                contentType = ContentType.Text.Html
            )
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

