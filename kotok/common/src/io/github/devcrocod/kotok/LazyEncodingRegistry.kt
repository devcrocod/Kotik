package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import io.github.devcrocod.kotok.api.EncodingType
import io.github.devcrocod.kotok.api.ModelType


internal class LazyEncodingRegistry : AbstractEncodingRegistry() {

    override fun encoding(encodingName: String): Encoding? {
        EncodingType.of(encodingName)?.let { type: EncodingType ->
            this.addEncoding(type)
        }
        return super.encoding(encodingName)
    }

    override fun encoding(encodingType: EncodingType): Encoding {
        addEncoding(encodingType)
        return super.encoding(encodingType)
    }

    override fun encodingForModel(modelType: ModelType): Encoding {
        addEncoding(modelType.encodingType)
        return super.encodingForModel(modelType)
    }
}