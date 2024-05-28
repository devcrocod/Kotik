package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import jdk.incubator.vector.ByteVector
import java.nio.ByteOrder

import java.util.concurrent.ConcurrentHashMap

internal actual fun concurrentMap(): MutableMap<String, Encoding> = ConcurrentHashMap()

fun test() {
    val s = "My string"
    val vector = ByteVector.SPECIES_512.fromByteArray(s.encodeToByteArray(), 0, ByteOrder.nativeOrder())
}
