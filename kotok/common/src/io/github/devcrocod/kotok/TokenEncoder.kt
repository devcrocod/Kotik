package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.TokenEncoderLarge.calculateTokensLarge
import io.github.devcrocod.kotok.api.TokenArray

public const val VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY: String = "VERY_LARGE_TOKENIZER_BYTE_THRESHOLD"

internal expect fun getProperty(key: String, defaultValue: Int): Int

internal class TokenEncoder(encoder: Map<ByteArray, Int>) {


    private val encoders: List<Map<ByteArrayWrapper, Int>>
    private val decoder: MutableMap<Int, ByteArray>

    private val veryLargeTokenizerByteThreshold: Int = if (encoder.isNotEmpty())
        getProperty(VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY, 500)
    else
        0

    companion object {
        internal const val MAX_RANK = Int.MAX_VALUE - 1
        internal const val DUMMY_RANK = Int.MAX_VALUE
    }

    init {
        if (encoder.isNotEmpty()) {
            val tempEncoders = mutableMapOf<Int, MutableMap<ByteArrayWrapper, Int>>()
            encoder.forEach { (key, value) ->
                val byteArrayWrapper = ByteArrayWrapper(key)
                val size = key.size
                val mapForSize = tempEncoders.getOrPut(size) { mutableMapOf() }
                mapForSize[byteArrayWrapper] = value
            }

            val maxSize = tempEncoders.keys.maxOrNull() ?: 0
            encoders = List(maxSize + 1) { tempEncoders[it] ?: emptyMap() }

            decoder = encoder.entries.associate { (key, value) -> value to key }.toMutableMap()
        } else {
            encoders = emptyList()
            decoder = emptyMap<Int, ByteArray>().toMutableMap()
        }
    }


    private fun encode(payload: ByteArrayWrapper): Int =
        encoders.getOrNull(payload.size)?.get(payload) ?: MAX_RANK

    internal fun encode(piece: ByteArrayWrapper, start: Int, end: Int): Int =
        if (end > piece.size || end - start == piece.size) {
            MAX_RANK
        } else {
            encode(piece.getBytesBetween(start, end))
        }

    fun addTokensAndGetCount(
        maxTokenCount: Int, keepEncodings: Boolean,
        byteArray: ByteArray, out: TokenArray, ranks: TokenArray
    ): Int {
        val match = ByteArrayWrapper(byteArray)
        val encoded = encode(match)
        return if (encoded != MAX_RANK) {
            if (keepEncodings) {
                out.add(encoded)
            }
            1
        } else {
            if (match.size < veryLargeTokenizerByteThreshold) {
                calculateTokensSmall(maxTokenCount, keepEncodings, out, ranks, match)
            } else {
                calculateTokensLarge(this, maxTokenCount, keepEncodings, out, match)
            }
        }
    }

    private fun calculateTokensSmall(
        maxTokenCount: Int, keepEncodings: Boolean,
        out: TokenArray, ranks: TokenArray, match: ByteArrayWrapper
    ): Int {
        val length = match.size
        require(length > 1) { "Already filtered out" }
        ranks.clear()
        ranks.ensureCapacity(length + 1)

        var minRankIndex = -1
        var minRank = MAX_RANK
        for (i in 0..length) {
            val encoded = encode(match, i, i + 2)
            if (encoded != MAX_RANK && encoded < minRank) {
                minRankIndex = i
                minRank = encoded
            }
            ranks.add(encoded)
        }

        val tokenCount = mergeBytesAndGetTokenCount(match, length, ranks, minRankIndex)
        if (keepEncodings) {
            var start = 0
            for (end in 1 until ranks.size) {
                if (ranks[end] != DUMMY_RANK) {
                    val token = encode(match, start, end)
                    require(token != MAX_RANK) { "Token should not be MAX_RANK" }
                    out.add(token)
                    start = end
                }
                if (out.size >= maxTokenCount) break
            }
        }
        return tokenCount
    }

    private fun mergeBytesAndGetTokenCount(
        piece: ByteArrayWrapper, length: Int, ranks: TokenArray, minRankIndex: Int
    ): Int {
        var lengthVar = length
        var minRankIdx = minRankIndex
        while (minRankIdx >= 0) {
            val previousIndex = getPreviousIndex(ranks, minRankIdx - 1)
            val nextIndex = getNextIndex(ranks, minRankIdx + 1)
            val nextNextIndex = getNextIndex(ranks, nextIndex + 1)
            val nextNextNextIndex = getNextIndex(ranks, nextNextIndex + 1)

            if (previousIndex >= 0) {
                val newRank = encode(piece, previousIndex, nextNextIndex)
                ranks[previousIndex] = newRank
            }

            val newRank = encode(piece, minRankIdx, nextNextNextIndex)
            ranks[minRankIdx] = newRank
            ranks[nextIndex] = DUMMY_RANK

            lengthVar--
            if (lengthVar < 3) break else minRankIdx = getMinRankIndex(ranks)
        }
        return lengthVar
    }

    private fun getMinRankIndex(ranks: TokenArray): Int {
        var minRankIndex = -1
        var minRank = MAX_RANK

        val length = ranks.size - 3
        var i = 0
        while (i < length - 2) {
            var r = ranks[i]
            if (r < minRank) {
                minRankIndex = i
                minRank = r
            }
            r = ranks[i + 1]
            if (r < minRank) {
                minRankIndex = i + 1
                minRank = r
            }
            r = ranks[i + 2]
            if (r < minRank) {
                minRankIndex = i + 2
                minRank = r
            }
            r = ranks[i + 3]
            if (r < minRank) {
                minRankIndex = i + 3
                minRank = r
            }
            i += 4
        }

        for (ind in i..length) {
            val r = ranks[ind]
            if (r < minRank) {
                minRankIndex = ind
                minRank = r
            }
        }
        return minRankIndex
    }

    private fun getNextIndex(ranks: TokenArray, nextIndex: Int): Int {
        var idx = nextIndex
        while (idx < ranks.size && ranks[idx] == DUMMY_RANK) {
            idx++
        }
        return idx
    }

    private fun getPreviousIndex(ranks: TokenArray, previousIndex: Int): Int {
        var idx = previousIndex
        while (idx >= 0 && ranks[idx] == DUMMY_RANK) {
            idx--
        }
        return idx
    }

    fun decodeToken(token: Int, specialEncoder: SpecialEncoder): ByteArray {
        return decoder.getOrPut(token) { specialEncoder.decodeIfPresent(token) ?: ByteArray(0) }
    }
}