package io.github.devcrocod.kotok

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Source

private val urls = mapOf(
    "/io/github/devcrocod/cl100k_base.tiktoken" to "https://raw.githubusercontent.com/devcrocod/kotok/main/kotok/src@jvmA/io/github/devcrocod/cl100k_base.tiktoken",
    "/io/github/devcrocod/p50k_base.tiktoken" to "https://raw.githubusercontent.com/devcrocod/kotok/main/kotok/src@jvmA/io/github/devcrocod/p50k_base.tiktoken",
    "/io/github/devcrocod/r50k_base.tiktoken" to "https://raw.githubusercontent.com/devcrocod/kotok/main/kotok/src@jvmA/io/github/devcrocod/r50k_base.tiktoken"
)

internal actual fun String.compileRegex(caseInsensitive: Boolean): Regex =
    Regex(this, if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet())

internal actual fun getResource(fileName: String): Source {
    val client = HttpClient()
    return runBlocking { // TODO
        client.use { httpClient ->
            val buffer = Buffer()
            buffer.write(httpClient.get(urls[fileName]!!).readBytes())
            buffer
        }
    }
}