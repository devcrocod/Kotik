package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.TokenArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TokenArrayTest {

    @Test
    fun `test array list operations`() {
        val tokenArray = TokenArray()
        val standardList = ArrayList<Int>()
        val random = Random(42)

        assertTrue(tokenArray.isEmpty())

        for (i in 0 until 1000) {
            val element = random.nextInt()
            tokenArray.add(element)
            standardList.add(element)
            assertEquals(standardList.last(), tokenArray[tokenArray.size - 1])

            if (tokenArray.isNotEmpty() && random.nextBoolean()) {
                val randomIndex = random.nextInt(tokenArray.size)
                val newElement = random.nextInt()
                tokenArray[randomIndex] = newElement
                standardList[randomIndex] = newElement
                assertEquals(standardList[randomIndex], tokenArray[randomIndex])
            }

            assertEquals(standardList.size, tokenArray.size)
            assertEquals(standardList.isEmpty(), tokenArray.isEmpty())

            assertEquals(standardList, tokenArray.toList())
            assertEquals(standardList.toString(), tokenArray.toString())

            if (random.nextInt() % 10 == 0) {
                tokenArray.clear()
                standardList.clear()
                assertEquals(0, tokenArray.size)
            }
        }

        val element = random.nextInt()
        tokenArray.add(element)
        standardList.add(element)

        val intArray = tokenArray.toIntArray()
        assertEquals(standardList.size, intArray.size)
        assertTrue(standardList.toIntArray().contentEquals(intArray))

        val anotherTokenArray = TokenArray()
        standardList.forEach(anotherTokenArray::add)

        assertEquals(tokenArray, tokenArray)
        assertEquals(tokenArray, anotherTokenArray)
        assertEquals(tokenArray.hashCode(), anotherTokenArray.hashCode())

        assertNotEquals(tokenArray, Any())
        anotherTokenArray[0] = tokenArray[0] + 1
        assertNotEquals(tokenArray, anotherTokenArray)

        assertNotEquals(tokenArray, TokenArray())
    }
}