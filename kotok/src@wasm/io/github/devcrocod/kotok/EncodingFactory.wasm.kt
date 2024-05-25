package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.internal.resourceURLs
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.io.Source

private val resourceCache = mutableMapOf<String, Source?>()
private val loadingStatus = mutableMapOf<String, Boolean>()

internal actual fun String.compileRegex(caseInsensitive: Boolean): Regex =
    Regex(this, if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet())

internal actual fun getResource(fileName: String): Source {
    resourceCache[fileName]?.let {
        return it
    }

    if (loadingStatus[fileName] == true) throw IOException("Resource $fileName is being loaded. Please try again later.")

    loadResourceAsync(fileName)

    return resourceCache[fileName] ?: throw IOException("Resource $fileName not found")
}

@OptIn(DelicateCoroutinesApi::class)
private fun loadResourceAsync(fileName: String) {
    val client = HttpClient()
    loadingStatus[fileName] = true

    GlobalScope.launch {
        client.use { httpClient ->
            val buffer = kotlinx.io.Buffer()
            buffer.write(httpClient.get(resourceURLs[fileName]!!).readBytes())
            resourceCache[fileName] = buffer
        }
    }
}