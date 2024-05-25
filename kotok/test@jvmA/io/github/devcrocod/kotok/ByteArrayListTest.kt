package io.github.devcrocod.kotok

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ByteArrayListTest {
    private fun randomByte(random: Random): Byte =
        (random.nextInt() and 0xFF).toByte()


    @Test
    fun `test array list operations`() {
        val byteArrayList = ByteArrayList()
        val standardList = ArrayList<Byte>()
        val random = Random(42)

        assertTrue(byteArrayList.isEmpty())

        for (i in 0 until 1000) {
            val element = randomByte(random)
            byteArrayList.add(element)
            standardList.add(element)
            assertEquals(standardList.last(), byteArrayList[byteArrayList.size - 1])

            if (byteArrayList.isNotEmpty() && random.nextBoolean()) {
                val randomIndex = random.nextInt(byteArrayList.size)
                val newElement = randomByte(random)
                byteArrayList[randomIndex] = newElement
                standardList[randomIndex] = newElement
                assertEquals(standardList[randomIndex], byteArrayList[randomIndex])
            }

            assertEquals(standardList.size, byteArrayList.size)
            assertEquals(standardList.isEmpty(), byteArrayList.isEmpty())

            assertEquals(standardList, byteArrayList.toList())
            assertEquals(standardList.toString(), byteArrayList.toString())

            if (randomByte(random) % 10 == 0) {
                byteArrayList.clear()
                standardList.clear()
                assertEquals(0, byteArrayList.size)
            }
        }

        val element = randomByte(random)
        byteArrayList.add(element)
        standardList.add(element)

        val byteArray = byteArrayList.toByteArray()
        assertEquals(standardList.size, byteArray.size)
        assertTrue(standardList.toByteArray().contentEquals(byteArray))

        val anotherByteArrayList = ByteArrayList()
        standardList.forEach(anotherByteArrayList::add)

        assertEquals(byteArrayList, byteArrayList)
        assertEquals(byteArrayList, anotherByteArrayList)
        assertEquals(byteArrayList.hashCode(), anotherByteArrayList.hashCode())

        assertNotEquals(byteArrayList, Any())
        anotherByteArrayList[0] = (byteArrayList[0] + 1).toByte()
        assertNotEquals(byteArrayList, anotherByteArrayList)

        assertNotEquals(byteArrayList, ByteArrayList())
    }
}