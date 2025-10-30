package com.amazon.ivs.realtimecollab.core.handlers.networking

import com.amazon.ivs.realtimecollab.core.handlers.stage.Participant
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {
    @POST("meeting/join")
    suspend fun joinMeeting(@Body body: JoinMeetingRequest): JoinMeetingResponse

    @POST("chat/token/create")
    suspend fun createChatToken(@Body body: JoinChatRequest): JoinChatResponse

    @GET("meetings/{meetingId}")
    suspend fun getMeeting(@Path("meetingId") id: String): MeetingResponse
}

@Serializable
data class JoinChatRequest(
    val meetingId: String,
)

@Serializable
data class JoinChatResponse(
    val token: String,
    val sessionExpirationTime: String,
    val tokenExpirationTime: String
)

@Serializable
data class JoinMeetingRequest(
    val meetingId: String?,
)

@Serializable
data class MeetingResponse(
    val id: String,
    val alias: String,
    val stageArn: String,
    val createdAt: String,
    val isActive: Boolean,
    val participants: Participants,
)

@Serializable
data class Participants(
    val user: Map<String, MeetingParticipant>,
    val display: Map<String, MeetingParticipant>
)

@Serializable
data class MeetingParticipant(
    val id: String,
    val isPublishing: Boolean,
    val attributes: MeetingParticipantAttributes,
) {
    val isScreenSharing get() = attributes.participantGroup == "display"
}

@Serializable
data class MeetingParticipantAttributes(
    val name: String,
    val picture: String,
    val participantGroup: String,
)

@Serializable
data class JoinMeetingResponse(
    val stageConfigs: StageConfigs,
    val stageArn: String,
    val meetingId: String
)

@Serializable
data class StageConfigs(
    val user: ParticipantConfig,
    val display: ParticipantConfig,
)

@Serializable
data class ParticipantConfig(
    val token: String,
    val participantId: String,
    val participantGroup: String,
)

fun MeetingParticipant.asParticipant(isViewer: Boolean) = Participant(
    id = id,
    name = attributes.name,
    isScreenSharing = isScreenSharing,
    isViewer = isViewer,
)
