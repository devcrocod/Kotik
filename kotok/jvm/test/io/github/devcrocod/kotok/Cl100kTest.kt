package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class Cl100kTest {

    private val PUNCTUATION = "'\".,?!:()"
    private val LETTERS = generateUnicodeCategoryString(Cl100kParser::isLetter)
    private val NUMBERS = generateUnicodeCategoryString(Cl100kParser::isNumeric)
    private val WHITESPACES = generateUnicodeCategoryString(Cl100kParser::isWhitespace)
    private val NEWLINES = "\n\r"
    private val NOT_NEWLINE_OR_LETTER_OR_NUMERIC =
        generateUnicodeCategoryString(Cl100kParser::isNotNewlineOrLetterOrNumeric)
    private val NOT_WHITESPACE_OR_LETTER_OR_NUMERIC =
        generateUnicodeCategoryString(Cl100kParser::isNotWhitespaceOrLetterOrNumeric)
    private val SPECIAL = listOf("'s", "'t", "'re", "'ve", "'m", "'ll", "'d", "'ſ", "'x", "🤚🏾", "😩", "　", "½")
    private val ENCODING by lazy { EncodingFactory.cl100kBase() }

    private fun generateUnicodeCategoryString(characterProperty: (Int) -> Boolean): String {
        return (0..Int.MAX_VALUE)
            .filter { Character.isDefined(it) }
            .filter { characterProperty(it) }
            .map { it.toChar() }
            .joinToString("")
    }

    private fun normalizeStringForTesting(testString: String): String {
        return testString
            .replace("\r", "\\\\r")
            .replace("\n", "\\\\n")
            .replace(" ", "␣")
    }

    private fun rand() = Random

    fun getEncoding(): Encoding {
        return ENCODING
    }

    //    @Disabled
    @Test
    fun measureEncodingSpeeds() {
        val input = StringBuilder()
        val measurements = sortedMapOf<Int, Long>()

        val iterations = 20
        for (i in generateSequence(1.0) { (it + 1).coerceAtLeast(it * 1.01) }.takeWhile { it < 1000 }) {
            while (input.length < i.toInt()) {
                input.append("a")
            }
            val inputString = input.toString()

            repeat(10 * iterations) {
                val warmup = getEncoding().encode(inputString)
                assert(warmup.isNotEmpty())
            }

            val startTime = System.nanoTime()
            repeat(iterations) {
                val encodingResult = getEncoding().encode(inputString)
                assert(encodingResult.isNotEmpty())
            }
            val endTime = System.nanoTime()
            measurements[i.toInt()] = (endTime - startTime) / iterations
        }
        measurements.forEach { (i, t) -> println("$i\t$t") }
    }

    @Test
    fun testEdgeCaseRoundTrips() {
        val testStrings = listOf(
            "\n",
            " ",
            "a : b",
            "  a",
            "\n \n ",
            "\n \n",
            "\n ",
            "\n \n!",
            "\n \n   ",
            "\n  !",
            "\n A",
            "  \n\r  \r\n  \r \n  A\nA \n A",
            ",\n\n",
            " ***\n\n\n\n",
            "   !",
            "   A",
            "   0",
            "   *",
            "   \n!",
            "   \nA",
            "   \n0",
            "   \n*",
            "   \n !",
            "   \n A",
            "   \n 0",
            "   \n *",
            "Many words map to one token, but some don't: indivisible.\n\nUnicode characters like emojis may be split into many tokens containing the underlying bytes: 🤚🏾\n\nSequences of characters commonly found next to each other may be grouped together: 1234567890",
            "I paid $123,456 to 9876543210 people!",
            "Mixed script: 你好 world! 🌍",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "Unicode snowman: ☃️",
            "I'm:  0\n",
            "We'll meet at 3 o'clock.",
            "Hello, world! It's a beautiful day...",
            "In 2023, I'll be 25 years old.",
            "Hello \n\n World  !",
            " It's 2:30pm;\n\n\n\nlet's eat, sleep , and code!",
            "'Thank God, here it is.' But when we took up the trunk...",
            "What in the world are you doing???!!!",
            "user@example.com",
            "this is a 'quoted' word",
            "　　a",
            "'ſ",
            "'ſ𣶸𣄬Ƙ淚",
            "😩\n",
            "03½",
            "* ע",
            "مرحبا بالعالم! كيف حالك؟ 😎",
            "\u0000\uD81C\uDFE1 a\u0000b-\u0000\u0000\u0000 \u0000",
            "🌍 a",
            "(𥧙h",
            ", 𰉄",
            "  󵨐)",
            "ﮀ\n ",
            "😐𪶫X",
            "෫𞅄",
            "𬕹\n  ",
            " 😈b\n𠂢'ſ𠃪𠃡Ƙ淚",
            "𗭾  󻥹\n𡛬蛇",
            "こんにちは世界"
        )

        testStrings.forEachIndexed { i, testString ->
            println("Validating `${normalizeStringForTesting(testString)}`")

            val actualTokens = getEncoding().encode(testString)
            val decoded = getEncoding().decode(actualTokens)
            assertEquals(testString, decoded, decoded)
        }
    }

    @Test
    fun testEncodeRoundTripWithRandomStrings() {
        val singleTokenStrings = getAllTokens()
        (0 until 100_000).asSequence().forEach { _ ->
            val testString = generateRandomUtf8String(singleTokenStrings)

            val maxTokenCount = rand().nextInt(1, 2 * testString.length)
            val actualTokens = getEncoding().encode(testString)
            assertEquals(actualTokens.size, getEncoding().countTokens(testString))

            val decodedTokens = getEncoding().decode(actualTokens)
            assertEquals(testString, decodedTokens, decodedTokens)

            val actualTrimmedTokens = getEncoding().encode(testString, maxTokenCount).tokens
            val decodedTrimmedTokens = getEncoding().decode(actualTrimmedTokens)
            assertTrue(testString.startsWith(decodedTrimmedTokens))
        }
    }

    @Test
    fun testEncodeOrdinaryRoundTripWithRandomStrings() {
        val singleTokenStrings = getAllTokens()
        (0 until 100_000).asSequence().forEach { _ ->
            val testString = generateRandomUtf8String(singleTokenStrings)

            val maxTokenCount = rand().nextInt(1, 2 * testString.length)
            val actualTokens = getEncoding().encodeOrdinary(testString)
            assertEquals(actualTokens.size, getEncoding().countTokensOrdinary(testString))

            val decodedTokens = getEncoding().decode(actualTokens)
            assertEquals(testString, decodedTokens, decodedTokens)

            val actualTrimmedTokens = getEncoding().encodeOrdinary(testString, maxTokenCount).tokens
            val decodedTrimmedTokens = getEncoding().decode(actualTrimmedTokens)
            assertTrue(testString.startsWith(decodedTrimmedTokens))
        }
    }

    private fun getAllTokens(): List<String> {
        return EncodingFactory
//            .loadMergeableRanks("/io/github/devcrocod/kotok/cl100k_base.tiktoken")
            .loadMergeableRanks("io/github/devcrocod/kotok/cl100k_base.tiktoken")
            .keys.map { String(it, Charsets.UTF_8) }
    }

    private fun generateRandomUtf8String(singleTokenStrings: List<String>): String {
        var testString: String
        do {
            val length = rand().nextInt(1, 10)
            testString = (0 until length)
                .map { getRandomCharFromCategory(rand().nextInt(0, 20), singleTokenStrings) }
                .joinToString("")
        } while (!Charsets.UTF_8.newEncoder().canEncode(testString))
        return testString
    }

    private fun getRandomCharFromCategory(category: Int, singleTokenStrings: List<String>): Char {
        return when (category) {
            0 -> ' '
            1 -> ' '
            2, 3, 4 -> (if (rand().nextBoolean()) 'a' else 'A') + rand().nextInt('z' - 'a' + 1)
            5 -> PUNCTUATION[rand().nextInt(PUNCTUATION.length)]
            6, 7 -> NEWLINES[rand().nextInt(NEWLINES.length)]
            8 -> NUMBERS[rand().nextInt(NUMBERS.length)]
            9 -> WHITESPACES[rand().nextInt(WHITESPACES.length)]
            10, 11 -> LETTERS[rand().nextInt(LETTERS.length)]
            12, 13 -> NOT_NEWLINE_OR_LETTER_OR_NUMERIC[rand().nextInt(NOT_NEWLINE_OR_LETTER_OR_NUMERIC.length)]
            14 -> NOT_WHITESPACE_OR_LETTER_OR_NUMERIC[rand().nextInt(NOT_WHITESPACE_OR_LETTER_OR_NUMERIC.length)]
            15, 16 -> (0x1F600 + rand().nextInt(0x50)).toChar()
            17 -> SPECIAL[rand().nextInt(SPECIAL.size)][0]
            18 -> singleTokenStrings[rand().nextInt(singleTokenStrings.size)][0]
            19 -> generateSequence { rand().nextInt(0x000000, 0X10FFFF) }
                .first { Character.isDefined(it) }
                .toChar()

            else -> throw IllegalStateException()
        }
    }
}
