package io.github.devcrocod.kotok

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class ByteArrayWrapperTest {
    @Test
    fun `get bytes between returns correct slice of array`() {
        val byteArray = ByteArrayWrapper(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        assertEquals(ByteArrayWrapper(byteArrayOf(4, 5, 6)), (byteArray.getBytesBetween(3, 6)))
    }

    @Test
    fun `get bytes between throws when inclusive start index out of bounds`() {
        val byteArray = ByteArrayWrapper(byteArrayOf(1, 2, 3, 4, 5, 6))

        assertFailsWith<IndexOutOfBoundsException> { byteArray.getBytesBetween(-1, 6) }
        assertFailsWith<IndexOutOfBoundsException> { byteArray.getBytesBetween(9, 10) }
    }

    @Test
    fun `get bytes between throws when exclusive end index out of bounds`() {
        val byteArray = ByteArrayWrapper(byteArrayOf(1, 2, 3, 4, 5, 6))

        assertFailsWith<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, 7) }
        assertFailsWith<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, -1) }
    }

    @Test
    fun `get bytes between throws when start index is greater than end index`() {
        val byteArray = ByteArrayWrapper(byteArrayOf(1, 2, 3, 4, 5, 6))

        assertFailsWith<IndexOutOfBoundsException> { byteArray.getBytesBetween(3, 2) }
    }
}