package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.Cl100kParser.split
import io.github.devcrocod.kotok.api.Encoding
import io.github.devcrocod.kotok.api.GptBytePairEncodingParams
import io.github.devcrocod.kotok.api.TokenArray
import kotlinx.io.Source
import kotlinx.io.readLine
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


internal expect fun String.compileRegex(caseInsensitive: Boolean): Regex

internal expect fun getResource(fileName: String): Source

public object EncodingFactory {
    private const val ENDOFTEXT: String = "<|endoftext|>"
    private const val FIM_PREFIX: String = "<|fim_prefix|>"
    private const val FIM_MIDDLE: String = "<|fim_middle|>"
    private const val FIM_SUFFIX: String = "<|fim_suffix|>"
    private const val ENDOFPROMPT: String = "<|endofprompt|>"

    private val SPECIAL_TOKENS_CL100K_BASE: Map<String, Int> = mapOf(
        ENDOFTEXT to 100257,
        FIM_PREFIX to 100258,
        FIM_MIDDLE to 100259,
        FIM_SUFFIX to 100260,
        ENDOFPROMPT to 100276
    )

    private val SPECIAL_TOKENS_X50K_BASE: Map<String, Int> = mapOf(ENDOFTEXT to 50256)

    private val SPECIAL_TOKENS_P50K_EDIT: Map<String, Int> = mapOf(
        ENDOFTEXT to 50256,
        FIM_PREFIX to 50281,
        FIM_MIDDLE to 50282,
        FIM_SUFFIX to 50283
    )

    public fun r50kBase(): Encoding = from50kParameters(
        "r50k_base",
        "/io/github/devcrocod/r50k_base.tiktoken",
        SPECIAL_TOKENS_X50K_BASE
    )

    public fun p50kBase(): Encoding = from50kParameters(
        "p50k_base",
        "/io/github/devcrocod/p50k_base.tiktoken",
        SPECIAL_TOKENS_X50K_BASE
    )

    public fun p50kEdit(): Encoding = from50kParameters(
        "p50k_edit",
        "/io/github/devcrocod/p50k_base.tiktoken",
        SPECIAL_TOKENS_P50K_EDIT
    )

    public fun cl100kBase(): Encoding {
        val mergeableRanks = loadMergeableRanks("/io/github/devcrocod/cl100k_base.tiktoken")
        val params: GptBytePairEncodingParams =
            GptBytePairEncodingParams("cl100k_base", null, mergeableRanks, SPECIAL_TOKENS_CL100K_BASE)
        return Cl100kGptBytePairEncoding(params)
    }

    public fun fromParameters(parameters: GptBytePairEncodingParams): Encoding = GptBytePairEncoding(parameters)

    private fun from50kParameters(name: String, fileName: String, specialTokens: Map<String, Int>): Encoding {
        val regex = "'(?:[sdmt]|ll|ve|re)| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+".compileRegex(false)
        val mergeableRanks = loadMergeableRanks(fileName)
        val params = GptBytePairEncodingParams(name, regex.pattern, mergeableRanks, specialTokens)
        return fromParameters(params)
    }

    @OptIn(ExperimentalEncodingApi::class)
    internal fun loadMergeableRanks(fileName: String): Map<ByteArray, Int> {
        val mergeableRanks = mutableMapOf<ByteArray, Int>()
        getResource(fileName).use { source: Source ->
            var line = source.readLine()
            while (line != null) {
                val firstSpaceIndex = line.indexOf(' ')
                require(firstSpaceIndex != -1) { "Invalid line in $fileName: $line" }

                val token = Base64.decode(line.substring(0, firstSpaceIndex).encodeToByteArray())
                val rank = line.substring(firstSpaceIndex + 1).toInt()

                mergeableRanks[token] = rank
                line = source.readLine()
            }
        }
        return mergeableRanks
    }

    private class Cl100kGptBytePairEncoding(params: GptBytePairEncodingParams) : GptBytePairEncoding(params) {
        override fun encodeOrdinaryInternal(text: String, maxTokenCount: Int, keepEncodings: Boolean, out: TokenArray): Int {
            val tokenCount = intArrayOf(0) // TODO
            val ranks = TokenArray()
            split(text) { utf8BytesList ->
                tokenCount[0] += encoder.addTokensAndGetCount(maxTokenCount, keepEncodings, utf8BytesList.toByteArray(), out, ranks)
                tokenCount[0] >= maxTokenCount
            }
            return tokenCount[0]
        }
    }
}