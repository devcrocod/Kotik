package io.github.devcrocod.kotok

import kotlin.math.max

public class ByteArrayList private constructor(private var array: ByteArray) {
    public var size = 0
        private set

    public constructor() : this(10)

    public constructor(size: Int) : this(ByteArray(size))

    public fun clear() {
        size = 0
    }

    public fun add(element: Byte) {
        if (size >= array.size) resize()
        array[size++] = element
    }

    public operator fun get(index: Int): Byte = array[index]

    public operator fun set(index: Int, element: Byte) {
        array[index] = element
    }

    private fun resize() {
        ensureCapacity(max(1, array.size) * 2)
    }

    public fun ensureCapacity(targetSize: Int) {
        if (targetSize <= size) return
        val newArray = ByteArray(targetSize)
        if (size > 0) {
            array.copyInto(newArray, 0, 0, size)
        }
        array = newArray
    }

    public fun isEmpty(): Boolean = size == 0

    public fun isNotEmpty(): Boolean = size > 0

    public fun toByteArray(): ByteArray = array.copyOf(size)

    public fun toList(): List<Byte> = array.toList()

    public override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is ByteArrayList || size != other.size -> false
            else -> array.contentEquals(other.array)
        }
    }

    public override fun hashCode(): Int {
        var result = 1
        for (item in array) {
            result = 31 * result + item
        }
        return result
    }

    public override fun toString(): String {
        return array.contentToString()
    }
}