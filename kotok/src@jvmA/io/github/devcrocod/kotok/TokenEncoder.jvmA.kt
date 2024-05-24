package io.github.devcrocod.kotok

internal actual fun getProperty(key: String, defaultValue: Int): Int =
    System.getProperty(key, defaultValue.toString()).toInt()