package io.github.devcrocod.kotok


internal class SpecialEncoder(encoder: Map<String, Int>) {
    private val SPECIAL_START = "<|"
    private val SPECIAL_END = "|>"

    private val encodedToDecoded: Map<Int, String> =
        encoder.entries.associate { (key, value) ->
            require(key.contains(SPECIAL_START) && key.contains(SPECIAL_END)) {
                "Special tokens must contain <| and |> (but was $key)"
            }
            value to key
        }

    fun decodeIfPresent(encodedToken: Int): ByteArray? {
        val result = encodedToDecoded[encodedToken]
        return result?.encodeToByteArray()
    }

    fun checkForSpecialTokens(text: String) {
        if (text.contains(SPECIAL_START) && text.contains(SPECIAL_END)) {
            for (specialToken in encodedToDecoded.values) {
                if (text.contains(specialToken))
                    throw UnsupportedOperationException("Encoding special tokens is not supported.")
            }
        }
    }
}
