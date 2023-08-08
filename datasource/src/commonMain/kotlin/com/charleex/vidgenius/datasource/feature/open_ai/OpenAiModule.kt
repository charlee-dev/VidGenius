package com.charleex.vidgenius.datasource.feature.open_ai

import co.touchlab.kermit.Logger
import com.charleex.vidgenius.datasource.feature.open_ai.api.OpenAiApi
import com.charleex.vidgenius.datasource.feature.open_ai.api.OpenAiApiImpl
import com.charleex.vidgenius.datasource.feature.open_ai.api.OpenAiConfig
import com.charleex.vidgenius.datasource.feature.open_ai.client.createHttpClient
import com.charleex.vidgenius.datasource.feature.open_ai.model.ModelId
import com.charleex.vidgenius.datasource.utils.getIsDebugBuild
import org.koin.dsl.module

internal val openAiModule
    get() = module {
        val openAiConfig = OpenAiConfig()

        single<OpenAiApi> {
            OpenAiApiImpl(
                httpClient = createHttpClient(openAiConfig)
            )
        }
        single<ChatService> {
            ChatServiceImpl(
                requester = get(),
                modelId = ModelId("gpt-3.5-turbo"),
            )
        }
        single<OpenAiRepository> {
            if (getIsDebugBuild()) OpenAiRepositoryDebug().also { println("OpenAiRepository in DEBUG mode") }
            else OpenAiRepositoryImpl(
                logger = Logger.withTag(OpenAiRepository::class.simpleName!!),
                database = get(),
                chatService = get(),
            ).also { println("OpenAiRepository in RELEASE mode") }
        }
    }