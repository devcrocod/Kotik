package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import io.github.devcrocod.kotok.api.EncodingResult
import io.github.devcrocod.kotok.api.GptBytePairEncodingParams
import io.github.devcrocod.kotok.api.TokenArray


internal open class GptBytePairEncoding(params: GptBytePairEncodingParams) : Encoding {
    val encoder: TokenEncoder = TokenEncoder(params.encoder)
    override val name: String = params.name
    private val pattern: String? = params.pattern
    private val specialEncoder: SpecialEncoder = SpecialEncoder(params.specialTokensEncoder)

    override fun encode(text: String): TokenArray = encode(text, Int.MAX_VALUE).tokens

    override fun encode(text: String, maxTokenCount: Int): EncodingResult =
        encodeInternal(text, maxTokenCount, true).toEncodingResult()

    override fun encodeOrdinary(text: String): TokenArray = encodeOrdinary(text, Int.MAX_VALUE).tokens

    override fun encodeOrdinary(text: String, maxTokenCount: Int): EncodingResult =
        encodeOrdinaryInternal(text, maxTokenCount, true).toEncodingResult()

    override fun countTokens(text: String): Int = encodeInternal(text, Int.MAX_VALUE, false).tokenCount

    override fun countTokensOrdinary(text: String): Int =
        encodeOrdinaryInternal(text, Int.MAX_VALUE, false).tokenCount

    override fun decode(tokens: TokenArray): String = decodeBytes(tokens).decodeToString()

    override fun decodeBytes(tokens: TokenArray): ByteArray {
        val out = ByteArrayList(10 * tokens.size)
        for (i in 0 until tokens.size) {
            val decodedToken = decodeToken(tokens[i])
            for (b in decodedToken) {
                out.add(b)
            }
        }
        return out.toByteArray()
    }

    private fun encodeInternal(text: String?, maxTokenCount: Int, keepEncodings: Boolean): InternalResult {
        if (text == null) return InternalResult(TokenArray(0), truncated = false)

        specialEncoder.checkForSpecialTokens(text)
        return encodeOrdinaryInternal(text, maxTokenCount, keepEncodings)
    }

    private fun encodeOrdinaryInternal(text: String?, maxTokenCount: Int, keepEncodings: Boolean): InternalResult {
        if (text == null) return InternalResult(TokenArray(0), truncated = false)

        val out = TokenArray()
        val tokenCount = encodeOrdinaryInternal(text, maxTokenCount, keepEncodings, out)

        if (keepEncodings && maxTokenCount != Int.MAX_VALUE) {
            // Make sure we didn't break the multibyte character
            for (tokensToRemove in 0..out.size) {
                val size: Int = out.size - tokensToRemove
                val tokens: TokenArray = TokenArray(size)
                for (i in 0 until size) {
                    tokens.add(out[i])
                }
                val decoded = decode(tokens)
                if (text.startsWith(decoded)) {
                    // If a decoded text is equal to the head of the original text, we can safely return the tokens
                    return InternalResult(tokens, truncated = text.length > decoded.length)
                }
            }
        }

        return InternalResult(out, tokenCount, false)
    }

    protected open fun encodeOrdinaryInternal(
        text: String,
        maxTokenCount: Int,
        keepEncodings: Boolean,
        out: TokenArray
    ): Int {
        var tokenCount = 0
        val ranks = TokenArray() // reused to avoid allocations
        val regex = Regex(pattern ?: throw IllegalArgumentException("Pattern must not be null"))

        val matches = regex.findAll(text)
        for (match in matches) {
            if (tokenCount >= maxTokenCount) break
            val bytes = match.value.encodeToByteArray()
            tokenCount += encoder.addTokensAndGetCount(maxTokenCount, keepEncodings, bytes, out, ranks)
        }

        return tokenCount
    }

    private fun decodeToken(token: Int): ByteArray {
        val decodedToken: ByteArray = encoder.decodeToken(token, specialEncoder)
        return requireNotNull(decodedToken) { "Unknown token for decoding: $token" }
    }

    private class InternalResult constructor(
        private val tokens: TokenArray,
        tokenCount: Int = -1,
        private val truncated: Boolean
    ) {
        val tokenCount = if (tokenCount < 0) tokens.size else tokenCount

        fun toEncodingResult(): EncodingResult {
            check(tokens.size == tokenCount) {
                "Token count does not match token list size (tokenCount= $tokenCount, tokens size= ${tokens.size})"
            }
            return EncodingResult(tokens, truncated)
        }
    }
}