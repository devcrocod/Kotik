package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class Cl100kLargeTokenizerTest {

    public lateinit var encoding: Encoding

    @BeforeTest
    fun `before all`() {
        getProperty(VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY, 0)
        encoding = EncodingFactory.cl100kBase()
    }

    @AfterTest
    fun `after all`() {
        System.clearProperty(VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY)
    }
}