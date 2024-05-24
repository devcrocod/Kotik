package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.EncodingRegistry

public object Encodings {

    public fun defaultEncodingRegistry(): EncodingRegistry =
        DefaultEncodingRegistry().apply { initializeDefaultEncodings() }

    public fun lazyEncodingRegistry(): EncodingRegistry =
        LazyEncodingRegistry()
}