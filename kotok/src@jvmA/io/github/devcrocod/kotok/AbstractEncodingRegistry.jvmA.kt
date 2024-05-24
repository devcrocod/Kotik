package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import java.util.concurrent.ConcurrentHashMap

internal actual fun concurrentMap(): MutableMap<String, Encoding> = ConcurrentHashMap()
