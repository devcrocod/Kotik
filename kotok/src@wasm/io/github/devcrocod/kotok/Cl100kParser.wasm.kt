package io.github.devcrocod.kotok

internal actual fun String.codePointAt(index: Int): Int {
    if (index < 0 || index >= this.length) {
        throw IndexOutOfBoundsException("Index: $index, Length: ${this.length}")
    }
    val high = this[index].code
    if (high in 0xD800..0xDBFF && index + 1 < this.length) {
        val low = this[index + 1].code
        if (low in 0xDC00..0xDFFF) {
            return (high - 0xD800) * 0x400 + (low - 0xDC00) + 0x10000
        }
    }
    return high
}

internal actual fun String.isValidUtf8(): Boolean {
    this.encodeToByteArray(throwOnInvalidSequence = true)
    return true
}