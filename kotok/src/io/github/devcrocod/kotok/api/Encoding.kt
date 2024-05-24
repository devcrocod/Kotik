package io.github.devcrocod.kotok.api

public interface Encoding {

    /**
     * Name of the environment variable key to control when JTokkit should switch to a different tokenizer.
     * For all inputs below the given threshold, JTokkit uses a tokenizer that scales in quadratic time but
     * is faster for small inputs. For larger inputs, a linearly scaling tokenizer is used. Per default, when
     * this environment variable is not set, the threshold is configured accordingly to our benchmarks to be
     * near-optimal for a wide variety of use-cases, but if you have a very specialized input format, you may
     * want to experiment and benchmark with different input size thresholds.
     */
    val VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY: String // TODO
        get() = "VERY_LARGE_TOKENIZER_BYTE_THRESHOLD"

    /**
     * Returns the name of this encoding. This is the name which is used to identify
     * the encoding and must be unique for registration in the [EncodingRegistry].
     */
    val name: String

    /**
     * Encodes the given text into a [TokenArray] of token ids.
     *
     * This function converts the input text into an array of token ids, where each token id corresponds
     * to a specific token from the model's vocabulary.
     *
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * [UnsupportedOperationException].
     *
     * If you want to encode special tokens as ordinary text, use [encodeOrdinary].
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.encode("hello world")
     * // returns [15339, 1917]
     *
     * encoding.encode("hello <|endoftext|> world")
     * // raises an UnsupportedOperationException
     * ```
     *
     * @param text the text to encode
     * @return the array of token ids
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    fun encode(text: String): TokenArray

    /**
     * Encodes the given text into a [TokenArray] of token ids.
     *
     * This function converts the input text into an array of token ids, where each token id corresponds
     * to a specific token from the model's vocabulary. The list of token ids will be truncated if the number
     * of tokens exceeds the given `maxTokens` parameter. Note that it will try to keep characters together
     * that are encoded into multiple tokens. For example, if the text contains a character which is encoded
     * into 3 tokens, and due to the `maxTokens` parameter the last token of the character is truncated,
     * the first two tokens of the character will also be truncated. Therefore, the actual number of tokens
     * may be less than the given `maxTokens` parameter.
     *
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * [UnsupportedOperationException].
     *
     * If you want to encode special tokens as ordinary text, use [encodeOrdinary].
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.encode("hello world", 100)
     * // returns [15339, 1917]
     *
     * encoding.encode("hello <|endoftext|> world", 100)
     * // raises an UnsupportedOperationException
     * ```
     *
     * @param text the text to encode
     * @param maxTokenCount the maximum number of tokens to encode
     * @return the [EncodingResult] containing a array of token ids and whether the tokens were truncated due to the maxTokens parameter
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    fun encode(text: String, maxTokenCount: Int): EncodingResult

    /**
     * Encodes the given text into a [TokenArray] of token ids, ignoring special tokens.
     *
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.encodeOrdinary("hello world")
     * // returns [15339, 1917]
     *
     * encoding.encodeOrdinary("hello <|endoftext|> world")
     * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
     * ```
     *
     * @param text the text to encode
     * @return the array of token ids
     */
    fun encodeOrdinary(text: String): TokenArray

    /**
     * Encodes the given text into a [TokenArray] of token ids, ignoring special tokens.
     *
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     *
     * It will truncate the list of token ids if the number of tokens exceeds the
     * given `maxTokens` parameter. Note that it will try to keep characters together that are encoded into
     * multiple tokens. For example, if the text contains a character which is encoded into 3 tokens,
     * and due to the `maxTokens` parameter the last token of the character is truncated, the first two
     * tokens of the character will also be truncated. Therefore, the actual number of tokens may be
     * less than the given `maxTokens` parameter.
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.encodeOrdinary("hello world", 100)
     * // returns [15339, 1917]
     *
     * encoding.encodeOrdinary("hello <|endoftext|> world", 100)
     * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
     * ```
     *
     * @param text the text to encode
     * @param maxTokenCount the maximum number of tokens to encode
     * @return the [EncodingResult] containing a array of token ids and whether the tokens were truncated due to the maxTokens parameter
     */
    fun encodeOrdinary(text: String, maxTokenCount: Int): EncodingResult

    /**
     * Encodes the given text into a list of token ids and returns the number of tokens.
     * It is more performant than [encode].
     *
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * [UnsupportedOperationException].
     *
     * If you want to encode special tokens as ordinary text, use [countTokensOrdinary].
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.countTokens("hello world")
     * // returns 2
     *
     * encoding.countTokens("hello <|endoftext|> world")
     * // raises an UnsupportedOperationException
     * ```
     *
     * @param text the text to count tokens for
     * @return the number of tokens
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    fun countTokens(text: String): Int

    /**
     * Encodes the given text into a list of token ids and returns the number of tokens.
     * It is more performant than [encodeOrdinary].
     *
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.countTokensOrdinary("hello world")
     * // returns 2
     *
     * encoding.countTokensOrdinary("hello <|endoftext|> world")
     * // returns 8
     * ```
     *
     * @param text the text to count tokens for
     * @return the number of tokens
     */
    fun countTokensOrdinary(text: String): Int

    /**
     * Decodes the given array of token ids into a text.
     *
     * This method converts an array of token ids back into the original text.
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.decode(intArrayOf(15339, 1917))
     * // returns "hello world"
     *
     * encoding.decode(intArrayOf(15339, 1917, Int.MAX_VALUE))
     * // raises an IllegalArgumentException
     * ```
     *
     * @param tokens the array of token ids
     * @return the decoded text
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    fun decode(tokens: IntArray): String = decode(TokenArray(tokens, tokens.size))

    /**
     * Decodes the given array of token ids into a text.
     *
     * This method converts an array of token ids back into the original text.
     *
     * @param tokens the array of token ids
     * @return the decoded text
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    fun decode(tokens: TokenArray): String

    /**
     * Decodes the given array of token ids into a byte array.
     *
     * This method converts an array of token ids back into the original byte array.
     *
     * Example usage:
     * ```
     * val encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE)
     * encoding.decodeBytes(intArrayOf(15339, 1917))
     * // returns [104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100]
     *
     * encoding.decodeBytes(intArrayOf(15339, 1917, Int.MAX_VALUE))
     * // raises an IllegalArgumentException
     * ```
     *
     * @param tokens the array of token ids
     * @return the decoded byte array
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    fun decodeBytes(tokens: IntArray): ByteArray = decodeBytes(TokenArray(tokens, tokens.size))

    /**
     * Decodes the given array of token ids into a byte array.
     *
     * This method converts an array of token ids back into the original byte array.
     *
     * @param tokens the array of token ids
     * @return the decoded byte array
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    fun decodeBytes(tokens: TokenArray): ByteArray
} 