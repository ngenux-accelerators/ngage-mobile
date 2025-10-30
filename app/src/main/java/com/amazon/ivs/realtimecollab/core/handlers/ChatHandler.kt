package com.amazon.ivs.realtimecollab.core.handlers

import com.amazon.ivs.realtimecollab.core.handlers.stage.updateList
import com.amazonaws.ivs.chat.messaging.ChatRoom
import com.amazonaws.ivs.chat.messaging.ChatRoomListener
import com.amazonaws.ivs.chat.messaging.ChatToken
import com.amazonaws.ivs.chat.messaging.DisconnectReason
import com.amazonaws.ivs.chat.messaging.SendMessageCallback
import com.amazonaws.ivs.chat.messaging.entities.ChatError
import com.amazonaws.ivs.chat.messaging.entities.ChatEvent
import com.amazonaws.ivs.chat.messaging.entities.ChatMessage
import com.amazonaws.ivs.chat.messaging.entities.DeleteMessageEvent
import com.amazonaws.ivs.chat.messaging.entities.DisconnectUserEvent
import com.amazonaws.ivs.chat.messaging.logger.ChatLogLevel
import com.amazonaws.ivs.chat.messaging.requests.SendMessageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

private const val CHAT_REGION = "us-west-2"

object ChatHandler {
    private var _chatRoom: ChatRoom? = null
    private var _chatRoomListener: ChatRoomListener? = null
    private val _messages = MutableStateFlow(emptyList<StageMessage>())

    val messages = _messages.asStateFlow()

    fun joinRoom(
        chatToken: ChatToken,
    ) {
        _messages.update { emptyList() }
        _chatRoom?.disconnect()
        _chatRoom = ChatRoom(
            regionOrUrl = CHAT_REGION,
            tokenProvider = { it.onSuccess(chatToken) },
            maxReconnectAttempts = 0,
        )
        _chatRoomListener = object : ChatRoomListener {
            override fun onConnected(room: ChatRoom) { Timber.d("On connected: ${room.id}") }
            override fun onConnecting(room: ChatRoom) { Timber.d("On connecting: ${room.id}") }
            override fun onDisconnected(room: ChatRoom, reason: DisconnectReason) {
                Timber.d("On disconnected: ${room.id}, ${reason.name}")
            }
            override fun onUserDisconnected(room: ChatRoom, event: DisconnectUserEvent) {
                Timber.d("On user disconnected: ${event.userId}")
            }

            override fun onEventReceived(room: ChatRoom, event: ChatEvent) {
                Timber.d("On event received: ${room.id}, $event")
                val message = event.attributes?.get("message")
                val username = event.attributes?.get("username") ?: event.id
                val eventMessages = mutableListOf<StageMessage>()

                if (message != null) {
                    eventMessages.add(
                        StageMessage(
                            messageId = event.id,
                            username = username,
                            message = message
                        )
                    )
                }
                _messages.updateList { addAll(eventMessages) }
            }

            override fun onMessageDeleted(room: ChatRoom, event: DeleteMessageEvent) {
                Timber.d("On message deleted: ${room.id}, ${event.attributes}")
                _messages.updateList { remove(find { it.messageId == event.messageId }) }
            }

            override fun onMessageReceived(room: ChatRoom, message: ChatMessage) {
                Timber.d("Message received: $message")
                if (message.attributes?.get("type") == null) {
                    val messages = _messages.value.toMutableSet().apply {
                        add(message.toStageMessage())
                    }.toList()
                    _messages.update { messages }
                    return
                }

                // TODO: Handle this
                // val userId = PreferencesHandler.user?.asObject<User>()?.username ?: return
                //if (message.sender.userId == userId) return
            }
        }
        _chatRoom?.listener = _chatRoomListener
        _chatRoom?.logLevel = ChatLogLevel.INFO
        _chatRoom?.connect()
    }

    fun sendMessage(message: String) {
        if (_chatRoom?.state != ChatRoom.State.CONNECTED) {
            Timber.d("Failed to send message - chat room not connected")
            return
        }
        val chatMessageRequest = SendMessageRequest(content = message)
        Timber.d("Sending message: $chatMessageRequest")
        _chatRoom?.sendMessage(chatMessageRequest, object : SendMessageCallback {
            override fun onConfirmed(request: SendMessageRequest, response: ChatMessage) {
                Timber.d("Message sent: ${request.requestId}, ${response.content}")
            }
            override fun onRejected(request: SendMessageRequest, error: ChatError) {
                Timber.d("Message send rejected: ${request.requestId}, ${error.errorMessage}")
            }
        })
    }

    fun clearMessages() {
        _messages.update { emptyList() }
    }
}

data class StageMessage(
    val messageId: String,
    val username: String,
    val message: String,
)

fun ChatMessage.toStageMessage() = StageMessage(
    messageId = this.id,
    username = this.sender.attributes?.get("username") ?: this.sender.userId,
    message = this.content
)
