package io.github.devcrocod.kotok.api

/**
 * The EncodingRegistry is used to register custom encodings and to retrieve
 * encodings by name or type. The out-of-the-box supported encodings are registered automatically.
 */
public interface EncodingRegistry {

    /**
     * Returns the encoding with the given name, if it exists. Otherwise, returns null.
     * Prefer using [encoding] or [encodingForModel] for built-in encodings.
     *
     * @param encodingName the name of the encoding
     * @return the encoding, if it exists, or null if it does not exist
     */
    public fun encoding(encodingName: String): Encoding?

    /**
     * Returns the encoding with the given type.
     *
     * @param encodingType the type of the encoding
     * @return the encoding
     */
    public fun encoding(encodingType: EncodingType): Encoding

    /**
     * Returns the encoding that is used for the given model name, if it exists. Otherwise, returns null.
     * Prefer using [encodingForModel] for built-in encodings.
     *
     * Note that you can use this method to retrieve the correct encodings for snapshots of models, for
     * example "gpt-4-0314" or "gpt-3.5-turbo-0301".
     *
     * @param modelName the name of the model to get the encoding for
     * @return the encoding, if it exists, or null if it does not exist
     */
    public fun encodingForModel(modelName: String): Encoding?

    /**
     * Returns the encoding that is used for the given model type.
     *
     * @param modelType the model type
     * @return the encoding
     */
    public fun encodingForModel(modelType: ModelType): Encoding

    /**
     * Registers a new byte pair encoding with the given parameters. The encoding must be thread-safe.
     *
     * @param parameters the parameters for the encoding
     * @return the registry for method chaining
     * @throws IllegalArgumentException if the encoding name is already registered
     */
    public fun registerGptBytePairEncoding(parameters: GptBytePairEncodingParams): EncodingRegistry

    /**
     * Registers a new custom encoding. The encoding must be thread-safe.
     *
     * @param encoding the encoding
     * @return the registry for method chaining
     * @throws IllegalArgumentException if the encoding name is already registered
     */
    public fun registerCustomEncoding(encoding: Encoding): EncodingRegistry
}