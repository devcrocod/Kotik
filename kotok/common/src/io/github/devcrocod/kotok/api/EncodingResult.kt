package io.github.devcrocod.kotok.api

/**
 * The result of encoding operation.
 *
 * @property tokens the array of tokens ids
 * @property truncated true if the token array was truncated because the maximum token length was exceeded
 */
public class EncodingResult(public val tokens: TokenArray, public val truncated: Boolean) {
    override fun toString(): String {
        return "EncodingResult(tokens=$tokens, truncated=$truncated)"
    }
} 