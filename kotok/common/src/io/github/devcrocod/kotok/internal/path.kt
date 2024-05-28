package io.github.devcrocod.kotok.internal

internal const val pathToCl100kBase = "io/github/devcrocod/kotok/cl100k_base.tiktoken"
internal const val pathToO200kBase = "/io/github/devcrocod/kotok/o200k_base.tiktoken"
internal const val pathToP50kBase = "/io/github/devcrocod/kotok/p50k_base.tiktoken"
internal const val pathToR50kBase = "/io/github/devcrocod/kotok/r50k_base.tiktoken"
internal const val pathToVocab = "/io/github/devcrocod/kotok/vocab.bpe"

internal val resourceURLs: Map<String, String>
    get() = mapOf(
        pathToCl100kBase to "https://raw.githubusercontent.com/devcrocod/kotok/master/kotok/src@jvmA$pathToCl100kBase",
        pathToP50kBase to "https://raw.githubusercontent.com/devcrocod/kotok/master/kotok/src@jvmA$pathToP50kBase",
        pathToR50kBase to "https://raw.githubusercontent.com/devcrocod/kotok/master/kotok/src@jvmA$pathToR50kBase"
    )