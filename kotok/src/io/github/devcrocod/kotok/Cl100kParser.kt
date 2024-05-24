package io.github.devcrocod.kotok


internal expect fun String.codePointAt(index: Int): Int

internal expect fun String.isValidUtf8(): Boolean

internal inline fun Int.charCount() = if (this >= 0x010000) 2 else 1


internal object Cl100kParser {
    private val SDTM = "sdtmSDTMſ"
    private val SIMPLE_WHITESPACES: String = "\t\n\u000B\u000C\r"
    private val REMAINING_WHITESPACES: IntArray = charArrayOf(
        '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004',
        '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A',
        '\u2028', '\u2029', '\u202F', '\u205F', '\u3000'
    ).map { it.code }.sorted().toIntArray()


    fun split(input: String?, fragConsumer: (ByteArrayList) -> Boolean) {
        require(input != null && isValidUtf8(input)) { "Input is not UTF-8: $input" }
        val utf8Bytes = ByteArrayList()
        var finished = false
        var endIndex = 0

        while (endIndex < input.length && !finished) {
            val startIndex = endIndex
            val c0 = input.codePointAt(startIndex)
            val cc0 = if (c0 >= 0x010000) 2 else 1
            val nextIndex = startIndex + cc0
            val c1 = if (nextIndex < input.length) input.codePointAt(nextIndex) else -1

            when {
                (c0.toChar() == '\'') && c1 > 0 -> {
                    when {
                        isShortContraction(c1) -> {
                            endIndex += 2
                            finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                            continue
                        }

                        startIndex + 2 < input.length && isLongContraction(c1, input.codePointAt(startIndex + 2)) -> {
                            endIndex += 3
                            finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                            continue
                        }
                    }
                }

                (isNotNewlineOrLetterOrNumeric(c0) && isLetter(c1)) || isLetter(c0) -> {
                    endIndex += cc0
                    if (isLetter(c1)) {
                        endIndex += c1.charCount()
                        while (endIndex < input.length && isLetter(input.codePointAt(endIndex))) {
                            endIndex += input.codePointAt(endIndex).charCount()
                        }
                    }
                    finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                }

                isNumeric(c0) -> {
                    endIndex += cc0
                    if (isNumeric(c1)) {
                        endIndex += c1.charCount()
                        if (endIndex < input.length && isNumeric(input.codePointAt(endIndex))) {
                            endIndex += input.codePointAt(endIndex).charCount()
                        }
                    }
                    finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                }

                isNotWhitespaceOrLetterOrNumeric(c0) || (c0 == ' '.code && isNotWhitespaceOrLetterOrNumeric(c1)) -> {
                    endIndex += cc0
                    if (endIndex < input.length && isNotWhitespaceOrLetterOrNumeric(c1)) {
                        endIndex += c1.charCount()
                        while (endIndex < input.length && isNotWhitespaceOrLetterOrNumeric(input.codePointAt(endIndex))) {
                            endIndex += input.codePointAt(endIndex).charCount()
                        }
                    }
                    while (endIndex < input.length && isNewline(input.codePointAt(endIndex))) {
                        endIndex++
                    }
                    finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                }

                else -> {
                    require(isWhitespace(c0)) { "Invalid character: ${c0.toChar()}" }
                    var lastNewLineIndex = if (isNewline(c0)) endIndex else -1
                    endIndex += cc0
                    if (isWhitespace(c1)) {
                        lastNewLineIndex = if (isNewline(c1)) endIndex else lastNewLineIndex
                        endIndex += c1.charCount()
                        while (endIndex < input.length && isWhitespace(input.codePointAt(endIndex))) {
                            lastNewLineIndex =
                                if (isNewline(input.codePointAt(endIndex))) endIndex else lastNewLineIndex
                            endIndex += input.codePointAt(endIndex).charCount()
                        }
                    }

                    if (lastNewLineIndex > -1) {
                        val finalEndIndex = endIndex
                        endIndex = lastNewLineIndex + 1
                        if (endIndex < finalEndIndex) {
                            require(startIndex < endIndex)
                            finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                            endIndex = finalEndIndex
                        }
                    }

                    if (!finished) {
                        if (lastNewLineIndex + 1 < endIndex && !isWhitespace(c0)) {
                            endIndex--
                        }
                        if (startIndex < endIndex) {
                            finished = fragConsumer(addUtf8Bytes(input, startIndex, endIndex, utf8Bytes))
                        }
                    }
                }
            }
        }
    }

    fun isShortContraction(ch: Int) = SDTM.indexOf(ch.toChar()) >= 0

    fun isLongContraction(ch1: Int, ch2: Int): Boolean {
        if ((((ch1 == 'l'.code) && (ch2 == 'l'.code))
                    || ((ch1 == 'v'.code) && (ch2 == 'e'.code))
                    || ((ch1 == 'r'.code) && (ch2 == 'e'.code)))
        ) {
            return true
        } else {
            val uch1: Int = ch1.toChar().uppercaseChar().code
            val uch2: Int = ch2.toChar().uppercaseChar().code
            return (((uch1 == 'L'.code) && (uch2 == 'L'.code))
                    || ((uch1 == 'V'.code) && (uch2 == 'E'.code))
                    || ((uch1 == 'R'.code) && (uch2 == 'E'.code)))
        }
    }

    fun isValidUtf8(input: String): Boolean = input.isValidUtf8()

    fun isLetter(ch: Int): Boolean = ch.toChar().isLetter()

    fun isNumeric(ch: Int): Boolean = ch.toChar().isDigit()

    fun isLetterOrNumeric(ch: Int): Boolean = ch.toChar().isLetterOrDigit()

    fun isWhitespace(ch: Int): Boolean = ch.toChar().isWhitespace()

    fun isNewline(ch: Int): Boolean = ((ch == '\r'.code) || (ch == '\n'.code))

    fun isNotWhitespaceOrLetterOrNumeric(ch: Int): Boolean =
        if (ch < '0'.code) {
            ch >= 0 && ch != ' '.code && (ch > '\r'.code || ch < '\t'.code)
        } else {
            !isLetterOrNumeric(ch) && !isWhitespace(ch)
        }

    fun isNotNewlineOrLetterOrNumeric(ch: Int): Boolean =
        if (ch < '0'.code) {
            ch >= 0 && (ch == ' '.code || !isNewline(ch))
        } else {
            !isLetterOrNumeric(ch)
        }

    private fun addUtf8Bytes(input: String, start: Int, end: Int, dst: ByteArrayList): ByteArrayList {
        dst.clear()
        var i = start
        while (i < end) {
            val cp = input.codePointAt(i)
            when {
                cp < 0x80 -> dst.add(cp.toByte())
                cp < 0x800 -> {
                    dst.add((0xc0 or (cp shr 6)).toByte())
                    dst.add((0x80 or (cp and 0x3f)).toByte())
                }

                cp < 0x010000 -> {
                    dst.add((0xe0 or (cp shr 12)).toByte())
                    dst.add((0x80 or ((cp shr 6) and 0x3f)).toByte())
                    dst.add((0x80 or (cp and 0x3f)).toByte())
                }

                else -> {
                    require(cp <= 0X10FFFF) { "Invalid code point: $cp" }
                    dst.add((0xf0 or (cp shr 18)).toByte())
                    dst.add((0x80 or ((cp shr 12) and 0x3f)).toByte())
                    dst.add((0x80 or ((cp shr 6) and 0x3f)).toByte())
                    dst.add((0x80 or (cp and 0x3f)).toByte())
                    i++ // Skip the low surrogate in the next iteration
                }
            }
            i++
        }
        return dst
    }
}