package io.github.devcrocod.kotok

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

internal actual fun String.codePointByIndex(index: Int): Int {
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

@OptIn(BetaInteropApi::class)
internal actual fun String.isValidUtf8(): Boolean {
    val nsStr = NSString.create(string = this)
    return nsStr.dataUsingEncoding(NSUTF8StringEncoding) != null
}