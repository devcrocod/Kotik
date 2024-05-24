package io.github.devcrocod.kotok.api

/**
 * Parameters for the byte pair encoding used to tokenize for the OpenAI GPT models.
 *
 * This library supports the encodings that are listed in [EncodingType] out of the box.
 * But if you want to use a custom encoding, you can use this class to pass the parameters to the library.
 * Use [EncodingRegistry.registerGptBytePairEncoding] to register your custom encoding
 * to the registry, so that you can easily use your encoding in conjunction with the predefined ones.
 *
 * The encoding parameters are:
 * - `name`: The name of the encoding. This is used to identify the encoding and must be unique.
 * - `pattern`: The pattern that is used to split the input text into tokens.
 * - `encoder`: The encoder that maps the tokens to their ids.
 * - `specialTokensEncoder`: The encoder that maps the special tokens to their ids.
 *
 * @property name the name of the encoding. This is used to identify the encoding and must be unique.
 * @property pattern the pattern that is used to split the input text into tokens.
 * @property encoder the encoder that maps the tokens to their ids. Note that the keys of this map
 *                   should be `ByteArray` objects.
 * @property specialTokensEncoder the encoder that maps the special tokens to their ids.
 */
public data class GptBytePairEncodingParams(
    public val name: String,
    public val pattern: String?,
    public val encoder: Map<ByteArray, Int>, // TODO
    public val specialTokensEncoder: Map<String, Int>
)