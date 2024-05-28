package io.github.devcrocod.kotok

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun getProperty(key: String, defaultValue: Int): Int =
    getenv(key)?.toKString()?.toIntOrNull() ?: defaultValue