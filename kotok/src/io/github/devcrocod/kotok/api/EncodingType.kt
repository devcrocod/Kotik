package io.github.devcrocod.kotok.api

public enum class EncodingType(private val typeName: String) {
    R50K_BASE("r50k_base"),
    P50K_BASE("p50k_base"),
    P50K_EDIT("p50k_edit"),
    CL100K_BASE("cl100k_base");

    public fun getName() = typeName

    companion object {
        public fun of(name: String): EncodingType? = entries.find { it.getName() == name }
    }
}
