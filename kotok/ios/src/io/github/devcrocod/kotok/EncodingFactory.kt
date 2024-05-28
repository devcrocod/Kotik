package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.internal.resourceURLs
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.Source


internal actual fun String.compileRegex(caseInsensitive: Boolean): Regex =
    Regex(this, if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet())

internal actual fun getResource(fileName: String): Source {
    val client = HttpClient()
    return runBlocking { // TODO
        client.use { httpClient ->
            val buffer = Buffer()
            buffer.write(httpClient.get(resourceURLs[fileName]!!).readBytes())
            buffer
        }
    }
}