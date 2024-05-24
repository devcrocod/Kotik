package io.github.devcrocod.kotok

import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.util.regex.Pattern


internal actual fun String.compileRegex(caseInsensitive: Boolean): Regex {
    return try {
        val flags =
            if (caseInsensitive) Pattern.UNICODE_CHARACTER_CLASS or Pattern.CASE_INSENSITIVE else Pattern.UNICODE_CHARACTER_CLASS
        Regex(Pattern.compile(this, flags).pattern())
    } catch (e: IllegalArgumentException) {
        // Workaround for Android
        val flags = if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0
        Regex(Pattern.compile(this, flags).pattern())
    }
}

internal actual fun getResource(fileName: String): Source {
    return Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)?.asSource()?.buffered()
        ?: throw IOException("Resource $fileName not found")
}