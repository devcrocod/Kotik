package io.github.devcrocod.kotok

import java.nio.charset.StandardCharsets

internal actual fun String.codePointByIndex(index: Int): Int = this.codePointAt(index)

internal actual fun String.isValidUtf8(): Boolean = StandardCharsets.UTF_8.newEncoder().canEncode(this)