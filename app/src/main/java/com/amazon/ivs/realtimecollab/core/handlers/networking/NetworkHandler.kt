package com.amazon.ivs.realtimecollab.core.handlers.networking

import com.amazon.ivs.realtimecollab.BuildConfig
import com.amazon.ivs.realtimecollab.R
import com.amazon.ivs.realtimecollab.core.handlers.AuthHandler
import com.amazon.ivs.realtimecollab.core.handlers.ChatHandler
import com.amazon.ivs.realtimecollab.core.handlers.Destination
import com.amazon.ivs.realtimecollab.core.handlers.NavigationHandler
import com.amazon.ivs.realtimecollab.core.handlers.stage.toDate
import com.amazonaws.ivs.chat.messaging.ChatToken
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val REQUEST_TIMEOUT = 30L

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

data class MeetingConfig(
    val meetingId: String = "",
    val userToken: String = "",
    val displayToken: String = "",
    val isVoiceOnly: Boolean = false,
)

object NetworkHandler {
    private val client: OkHttpClient = run {
        val builder = OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val updatedRequest = chain
                    .request()
                    .newBuilder()
                    .addHeader(name = "Accept", value = "application/json")
                    .addHeader(name = "Authorization", value = AuthHandler.user.value.token ?: "")
                    .build()
                chain.proceed(updatedRequest)
            }
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }
        builder.build()
    }
    private val api = Retrofit.Builder()
        .client(client)
        .baseUrl(BuildConfig.AUTH_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(Api::class.java)

    suspend fun joinMeeting(meetingId: String?, isVoiceOnly: Boolean = false): MeetingConfig? = try {
        val meetingResponse = makeRetriableRequest {
            api.joinMeeting(
                body = JoinMeetingRequest(
                    meetingId = meetingId,
                    sessionType = if (meetingId == null) (if (isVoiceOnly) "voice" else "video") else null,
                )
            )
        }
        Timber.d("Meeting joined: $meetingResponse")
        val chatResponse = makeRetriableRequest {
            api.createChatToken(
                body = JoinChatRequest(
                    meetingId = meetingResponse.meetingId
                )
            )
        }
        Timber.d("Chat joined: $chatResponse")
        ChatHandler.joinRoom(chatToken = chatResponse.asChatToken())
        NavigationHandler.goTo(Destination.JoinScreen)
        MeetingConfig(
            meetingId = meetingResponse.meetingId,
            userToken = meetingResponse.stageConfigs.user.token,
            displayToken = meetingResponse.stageConfigs.display.token,
            isVoiceOnly = meetingResponse.sessionType == "voice",
        )
    } catch (e: Exception) {
        Timber.w(e, "Failed to join stage")
        NavigationHandler.showError(error = R.string.err_join_stage)
        null
    }

    suspend fun getMeetingParticipants(meetingId: String) = try {
        makeRetriableRequest {
            val response = api.getMeeting(id = meetingId)
            Timber.d("Meeting info received: $response")
            Timber.d("Participants: ${response.participants.user.values.map { "{${it.attributes.name}, ${it.attributes.participantGroup}, ${it.isPublishing}}" }}")
            Timber.d("Display: ${response.participants.display.values.map { "{${it.attributes.name}, ${it.attributes.participantGroup}, ${it.isPublishing}}" }}")
            response.participants.user.values.map { it.asParticipant(isViewer = !it.isPublishing) }
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to get stage members")
        null
    }

    private suspend fun <T> makeRetriableRequest(
        block: suspend () -> T
    ): T {
        val retryCode = 401
        val maxRetryCount = 30
        var retryCount = 0

        Timber.d("Making request")
        while (retryCount < maxRetryCount) {
            try {
                return block()
            } catch (e: Exception) {
                val code = (e as? HttpException)?.code()
                val isRetryCode = code == retryCode
                if (isRetryCode) {
                    AuthHandler.refreshToken()
                    delay(1000)
                } else {
                    Timber.d("Request failed after: $retryCount attempts")
                    error("Request failed")
                }
            }
            retryCount++
        }

        Timber.d("Request failed after: $retryCount attempts")
        error("Request failed")
    }
}

fun JoinChatResponse.asChatToken() = ChatToken(
    token = token,
    sessionExpirationTime = sessionExpirationTime.toDate(),
    tokenExpirationTime = tokenExpirationTime.toDate()
)
