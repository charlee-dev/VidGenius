package com.charleex.vidgenius.open_ai.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Creates a completion for the chat message.
 */
@Serializable
class ChatCompletionRequest(
    /**
     * ID of the model to use.
     */
    @SerialName("model") val model: String,

    /**
     * The messages to generate chat completions for.
     */
    @SerialName("messages") val messages: List<ChatMessage>,

    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     *
     * We generally recommend altering this or [topP] but not both.
     */
    @SerialName("temperature") val temperature: Double? = null,

    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results
     * of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass
     * are considered.
     *
     * We generally recommend altering this or [temperature] but not both.
     */
    @SerialName("top_p") val topP: Double? = null,

    /**
     * How many chat completion choices to generate for each input message.
     */
    @SerialName("n") val n: Int? = null,

    /**
     * Up to 4 sequences where the API will stop generating further tokens.
     */
    @SerialName("stop") val stop: List<String>? = null,

    /**
     * The maximum number of tokens allowed for the generated answer. By default, the number of tokens the model can
     * return will be (4096 - prompt tokens).
     */
    @SerialName("max_tokens") val maxTokens: Int? = null,

    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far,
     * increasing the model's likelihood to talk about new topics.
     *
     * [Read more](https://platform.openai.com/docs/api-reference/parameter-details)
     */
    @SerialName("presence_penalty") val presencePenalty: Double? = null,

    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so
     * far, decreasing the model's likelihood to repeat the same line verbatim.
     *
     * [Read more](https://platform.openai.com/docs/api-reference/parameter-details)
     */
    @SerialName("frequency_penalty") val frequencyPenalty: Double? = null,

    /**
     * Modify the likelihood of specified tokens appearing in the completion.
     *
     * Accepts a json object that maps tokens (specified by their token ID in the tokenizer) to an associated bias value
     * from -100 to 100. Mathematically, the bias is added to the logits generated by the model prior to sampling.
     * The exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood of
     * selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant token.
     */
    @SerialName("logit_bias") val logitBias: Map<String, Int>? = null,

    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    @SerialName("user") public val user: String? = null,

    /**
     * A list of functions the model may generate JSON inputs for.
     */
    @SerialName("functions") val functions: List<ChatCompletionFunction>? = null,

    /**
     * Controls how the model responds to function calls. [FunctionMode.None] means the model does not call a function,
     * and responds to the end-user.
     * [FunctionMode.Auto] means the model can pick between an end-user or calling a function.
     * Specifying a particular function via [FunctionMode.Named] forces the model to call that function.
     * [FunctionMode.None] is the default when no functions are present.
     * [FunctionMode.Auto] is the default if functions are present.
     */
    @SerialName("function_call") val functionCall: FunctionMode? = null,
)