package src.charleex.autoytvid.repository

import com.hackathon.cda.utils.measureTimeMillisPair
import kotlinx.coroutines.flow.Flow
import okio.FileSystem
import src.charleex.autoytvid.api.monto_api.MontoApi
import src.charleex.autoytvid.repository.model.AudioTranscription
import src.charleex.autoytvid.repository.model.Message
import src.charleex.autoytvid.repository.model.Role
import src.charleex.autoytvid.whisper.ChatService
import src.charleex.autoytvid.whisper.TranscriptionService
import src.charleex.autoytvid.whisper.TranslationService
import src.charleex.autoytvid.whisper.model.chat.ChatCompletionChunk
import src.charleex.autoytvid.whisper.model.chat.ChatCompletionFunction
import src.charleex.autoytvid.whisper.model.chat.ChatMessage
import src.charleex.autoytvid.whisper.model.chat.FunctionMode

interface AssistRepository {
    suspend fun transcryptAudio(
        messageId: Int,
        filePath: String,
        fileSystem: FileSystem = FileSystem.SYSTEM,
    ): AudioTranscription

    /**
     * Flow:
     * - transcrypt - audio-to-text - output original text and language name
     * - store language name, to be used by next transcription
     * - translate - original text to english text - output english text
     * - send CrashMessage to the api
     * - receive an answer in the original language from the api
     * - return the 'Answer' with 'message'
     */
    suspend fun sendMessage(
        crashId: String,
        audioTranscription: AudioTranscription,
        latitude: Double,
        longitude: Double,
    ): Message

    suspend fun chat(
        messageId: Int,
        messages: List<ChatMessage> = emptyList(),
        temperature: Double? = null,
        topP: Double? = null,
        n: Int? = null,
        stop: List<String>? = null,
        maxTokens: Int? = null,
        presencePenalty: Double? = null,
        frequencyPenalty: Double? = null,
        logitBias: Map<String, Int>? = null,
        user: String? = null,
        functions: List<ChatCompletionFunction>? = null,
        functionCall: FunctionMode? = null,
    ): Message

    fun chats(
        messages: List<ChatMessage> = emptyList(),
        temperature: Double? = null,
        topP: Double? = null,
        n: Int? = null,
        stop: List<String>? = null,
        maxTokens: Int? = null,
        presencePenalty: Double? = null,
        frequencyPenalty: Double? = null,
        logitBias: Map<String, Int>? = null,
        user: String? = null,
        functions: List<ChatCompletionFunction>? = null,
        functionCall: FunctionMode? = null,
    ): Flow<ChatCompletionChunk>
}

internal class AssistRepositoryImpl(
    private val montoApi: MontoApi,
    private val transcriptionService: TranscriptionService,
    private val translationService: TranslationService,
    private val chatService: ChatService,
) : AssistRepository {
    private var conversationLanguage: String? = null

    override suspend fun transcryptAudio(
        messageId: Int,
        filePath: String,
        fileSystem: FileSystem,
    ): AudioTranscription {
        val transcription = measureTimeMillisPair {
            getTranscription(messageId, filePath, fileSystem)
        }

        conversationLanguage = transcription.first.language

        val translation = measureTimeMillisPair {
            getTranslation(filePath, fileSystem)
        }

        return AudioTranscription(
            id = transcription.first.id + 1,
            originalText = transcription.first.message,
            englishText = translation.first,
            language = transcription.first.language,
            transcriptionTime = transcription.second,
            translationTime = translation.second,
        )
    }

    override suspend fun sendMessage(
        crashId: String,
        audioTranscription: AudioTranscription,
        latitude: Double,
        longitude: Double,
    ): Message {
        val crashMessageResponse = measureTimeMillisPair {
            montoApi.sendCrashMessage(
                crashId = crashId,
                messageId = audioTranscription.id,
                originalText = audioTranscription.originalText,
                englishText = audioTranscription.englishText,
                language = audioTranscription.language ?: "english",
                latitude = latitude,
                longitude = longitude,
            )
        }
        println("[AssistRepository] ANSWER: $crashMessageResponse, TIME: ${crashMessageResponse.second}")
        return Message(
            id = audioTranscription.id + 1,
            message = crashMessageResponse.first.message,
            language = conversationLanguage,
            role = Role.AI,
            answerTime = crashMessageResponse.second,
        )
    }

    override suspend fun chat(
        messageId: Int,
        messages: List<ChatMessage>,
        temperature: Double?,
        topP: Double?,
        n: Int?,
        stop: List<String>?,
        maxTokens: Int?,
        presencePenalty: Double?,
        frequencyPenalty: Double?,
        logitBias: Map<String, Int>?,
        user: String?,
        functions: List<ChatCompletionFunction>?,
        functionCall: FunctionMode?,
    ): Message {
        val completion = measureTimeMillisPair {
            chatService.chatCompletion(
                messages = messages,
                temperature = temperature,
                topP = topP,
                n = n,
                stop = stop,
                maxTokens = maxTokens,
                presencePenalty = presencePenalty,
                frequencyPenalty = frequencyPenalty,
                logitBias = logitBias,
                user = user,
                functions = functions,
                functionCall = functionCall,
            )
        }
        println("[AssistRepository] COMPLETION: $completion, TIME: ${completion.second}")
        return Message(
            id = messageId,
            message = completion.first.choices.map { it.message }.joinToString(separator = "\n"),
            language = conversationLanguage,
            role = Role.AI,
            answerTime = completion.second,
        )
    }

    override fun chats(
        messages: List<ChatMessage>,
        temperature: Double?,
        topP: Double?,
        n: Int?,
        stop: List<String>?,
        maxTokens: Int?,
        presencePenalty: Double?,
        frequencyPenalty: Double?,
        logitBias: Map<String, Int>?,
        user: String?,
        functions: List<ChatCompletionFunction>?,
        functionCall: FunctionMode?,
    ): Flow<ChatCompletionChunk> {
        return chatService.chatCompletions(
            messages = messages,
            temperature = temperature,
            topP = topP,
            n = n,
            stop = stop,
            maxTokens = maxTokens,
            presencePenalty = presencePenalty,
            frequencyPenalty = frequencyPenalty,
            logitBias = logitBias,
            user = user,
            functions = functions,
            functionCall = functionCall,
        )
    }

    private suspend fun getTranscription(
        id: Int,
        filePath: String,
        fileSystem: FileSystem,
    ): Message {
        val transcription = measureTimeMillisPair {
            transcriptionService.transcriptAudio(
                filePath = filePath,
                fileSystem = fileSystem,
                language = conversationLanguage?.codeISO6391(),
            )
        }
        println("[AssistRepository] TRANSCRIPTION: $transcription, TIME: ${transcription.second}")
        conversationLanguage = transcription.first.language
        return Message(
            id = id,
            message = transcription.first.text,
            language = transcription.first.language,
            role = Role.USER,
            answerTime = transcription.second,
        )
    }

    private suspend fun getTranslation(
        filePath: String,
        fileSystem: FileSystem,
    ): String {
        val translation = measureTimeMillisPair {
            translationService.translateAudio(filePath, fileSystem)
        }
        println("[AssistRepository] TRANSLATION: $translation, TIME: ${translation.second}")
        return translation.first.text
    }
}

private fun String.codeISO6391() = when (this.lowercase()) {
    "english" -> "en"
    "spanish" -> "es"
    "french" -> "fr"
    "german" -> "de"
    "italian" -> "it"
    "japanese" -> "ja"
    "korean" -> "ko"
    "dutch" -> "nl"
    "portuguese" -> "pt"
    "russian" -> "ru"
    "chinese" -> "zh"
    "danish" -> "da"
    "finnish" -> "fi"
    "greek" -> "el"
    "hindi" -> "hi"
    "hungarian" -> "hu"
    "indonesian" -> "id"
    "norwegian" -> "no"
    "polish" -> "pl"
    "swedish" -> "sv"
    "thai" -> "th"
    "turkish" -> "tr"
    "czech" -> "cs"
    "arabic" -> "ar"
    else -> null
}