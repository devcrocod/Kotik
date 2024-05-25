package io.github.devcrocod.kotok.api

import kotlin.math.max

public class TokenArray internal constructor(private var array: IntArray, size: Int = 0 ) {

    /**
     * The number of elements in this array.
     */
    public var size = size
        private set

    /**
     * Creates an empty array with initial capacity of ten.
     */
    constructor() : this(10)

    /**
     * Creates an empty array with specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the array
     */
    constructor(initialCapacity: Int) : this(IntArray(initialCapacity + 1))

    /**
     * Removes all the elements from this array. The array will be empty after this call returns.
     */
    public fun clear(): Unit {
        size = 0
    }

    /**
     * Appends the specified element to the end of this array.
     *
     * @param element element to be appended to this array
     */
    public fun add(element: Int) {
        if (size >= array.size) {
            resize()
        }
        array[size++] = element
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (`index < 0 || index >= size()`)
     */
    public operator fun get(index: Int): Int = array[index]

    /**
     * Replaces the element at the specified position in this array.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (`index < 0 || index >= size()`)
     */
    public operator fun set(index: Int, element: Int): Int {
        val old = array[index]
        array[index] = element
        return old
    }

    private fun resize() {
        ensureCapacity(max(1, array.size) * 2)
    }


    /**
     * Ensures that the capacity of this array is at least equal to the specified [minCapacity].
     *
     * If the current capacity is less than the [minCapacity], a new backing storage is allocated with greater capacity.
     * Otherwise, this method takes no action and simply returns.
     *
     * @param minCapacity the minimum capacity, which must be greater than zero
     */
    public fun ensureCapacity(minCapacity: Int) {
        if (minCapacity <= size) return
        val newArray = IntArray(minCapacity)
        if (size > 0) {
            array.copyInto(newArray, 0, 0, size)
        }
        array = newArray
    }

    public fun isEmpty(): Boolean = size == 0

    public fun isNotEmpty(): Boolean = size > 0

    public fun toIntArray(): IntArray = array.copyOf(size)

    public fun toList(): List<Int> = array.copyOf(size).toList()

    public override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is TokenArray || size != other.size -> false
            else -> {
                var eq = true
                for (i in 0 until size) {
                    if (array[i] != other.array[i]) {
                        eq = false
                        break
                    }
                }
                eq
            }
        }
    }

    public override fun hashCode(): Int {
        var result = 1
        for (i in 0 until size) {
            result = 31 * result + array[i]
        }
        return result
    }
    
    public override fun toString(): String {
        return array.copyOf(size).asList().toString()
    }
}