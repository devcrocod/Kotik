package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.Encoding
import io.github.devcrocod.kotok.api.EncodingRegistry
import io.github.devcrocod.kotok.api.EncodingType
import io.github.devcrocod.kotok.api.GptBytePairEncodingParams
import io.github.devcrocod.kotok.api.ModelType

internal expect fun concurrentMap(): MutableMap<String, Encoding>

public abstract class AbstractEncodingRegistry : EncodingRegistry {
    private val encodings = concurrentMap()

    override fun encoding(encodingName: String): Encoding? = encodings[encodingName]

    override fun encoding(encodingType: EncodingType): Encoding = encodings.getValue(encodingType.getName())

    override fun encodingForModel(modelName: String): Encoding? {
        val modelType = ModelType.of(modelName)
        return when {
            modelType != null -> encodingForModel(modelType)
            modelName.startsWith(ModelType.GPT_4_32K.modelName) -> encodingForModel(ModelType.GPT_4_32K)
            modelName.startsWith(ModelType.GPT_4.modelName) -> encodingForModel(ModelType.GPT_4)
            modelName.startsWith(ModelType.GPT_3_5_TURBO_16K.modelName) -> encodingForModel(ModelType.GPT_3_5_TURBO_16K)
            modelName.startsWith(ModelType.GPT_3_5_TURBO.modelName) -> encodingForModel(ModelType.GPT_3_5_TURBO)
            else -> null
        }
    }

    override fun encodingForModel(modelType: ModelType): Encoding =
        encodings.getValue(modelType.encodingType.getName())

    override fun registerGptBytePairEncoding(parameters: GptBytePairEncodingParams): EncodingRegistry =
        registerCustomEncoding(EncodingFactory.fromParameters(parameters))

    override fun registerCustomEncoding(encoding: Encoding): EncodingRegistry {
        val encodingName = encoding.name
        val previousEncoding = encodings.getOrElse(encodingName) {
            encodings[encodingName] = encoding
            null
        }
        check(previousEncoding == null) { "Encoding $encodingName already registered" }
        return this
    }

    protected fun addEncoding(encodingType: EncodingType) {
        when (encodingType) {
            EncodingType.R50K_BASE -> encodings.getOrPut(encodingType.getName()) { EncodingFactory.r50kBase() }
            EncodingType.P50K_BASE -> encodings.getOrPut(encodingType.getName()) { EncodingFactory.p50kBase() }
            EncodingType.P50K_EDIT -> encodings.getOrPut(encodingType.getName()) { EncodingFactory.p50kEdit() }
            EncodingType.CL100K_BASE -> encodings.getOrPut(encodingType.getName()) { EncodingFactory.cl100kBase() }
        }
    }
}