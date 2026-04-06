package com.example.whatsapp.presentation.chatscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsapp.data.remote.BackendClient
import com.example.whatsapp.data.remote.BackendConfig
import com.example.whatsapp.data.remote.MessageDto
import com.example.whatsapp.data.remote.SendMessageRequestDto
import com.example.whatsapp.data.session.ChatSession
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class DeliveryStatus {
    SENT,
    DELIVERED,
    RECEIVED
}

data class UiChatMessage(
    val id: Long? = null,
    val localId: String = UUID.randomUUID().toString(),
    val senderId: Long,
    val content: String,
    val timestamp: String,
    val status: DeliveryStatus
)

class ChatViewModel : ViewModel() {

    private val dummyPhoneToId = mapOf(
        "9890989098" to 1L,
        "6262626262" to 2L,
        "8787878787" to 3L
    )

    private val gson = Gson()
    private val httpClient = OkHttpClient()
    private val wsCandidates = BackendConfig.wsCandidates()
    private var webSocket: WebSocket? = null
    private var activePeerId: Long? = null
    private var isConnectingSocket = false

    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus = _connectionStatus.asStateFlow()

    private var reconnectUserId: Long? = null
    private var historySyncJob: Job? = null

    fun openChat(otherPhone: String) {
        val myUserId = ChatSession.userId ?: dummyPhoneToId[ChatSession.phoneNumber]
        val fallbackPeerId = dummyPhoneToId[otherPhone]

        if (myUserId == null) {
            _connectionStatus.value = "Disconnected"
            return
        }

        reconnectUserId = myUserId
        _connectionStatus.value = "Connecting..."

        viewModelScope.launch {
            val token = ChatSession.token ?: return@launch
            runCatching {
                BackendClient.userApi.userByPhone(otherPhone, "Bearer $token")
            }.onSuccess { contact ->
                activePeerId = contact.id
                loadHistory(contact.id)
                startHistorySync(contact.id)
                connectSocket(myUserId)
            }.onFailure {
                activePeerId = fallbackPeerId
                if (activePeerId != null) {
                    _messages.value = emptyList()
                    startHistorySync(activePeerId!!)
                    connectSocket(myUserId)
                } else {
                    _connectionStatus.value = "Disconnected"
                }
            }
        }

        if (ChatSession.token.isNullOrBlank()) {
            activePeerId = fallbackPeerId
            if (activePeerId != null) {
                _messages.value = emptyList()
                startHistorySync(activePeerId!!)
                connectSocket(myUserId)
            }
        }
    }

    private fun loadHistory(otherUserId: Long) {
        val token = ChatSession.token ?: return

        viewModelScope.launch {
            runCatching {
                BackendClient.messageApi.history(otherUserId, "Bearer $token")
            }.onSuccess { history ->
                _messages.value = history.map { it.toUi() }
            }
        }
    }

    private fun connectSocket(myUserId: Long) {
        if (webSocket != null || isConnectingSocket) {
            return
        }

        isConnectingSocket = true
        connectSocketCandidate(myUserId, 0)
    }

    private fun connectSocketCandidate(myUserId: Long, candidateIndex: Int) {
        if (candidateIndex >= wsCandidates.size) {
            isConnectingSocket = false
            _connectionStatus.value = "Disconnected"
            scheduleReconnect()
            return
        }

        val candidateUrl = "${wsCandidates[candidateIndex]}?userId=$myUserId"
        _connectionStatus.value = "Connecting..."

        val request = Request.Builder()
            .url(candidateUrl)
            .build()

        httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(openedSocket: WebSocket, response: Response) {
                webSocket = openedSocket
                isConnectingSocket = false
                _connectionStatus.value = "Connected"
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val incoming = runCatching { gson.fromJson(text, MessageDto::class.java) }.getOrNull() ?: return
                val peerId = activePeerId ?: return
                val myId = ChatSession.userId ?: dummyPhoneToId[ChatSession.phoneNumber]

                val isRelevant =
                    (incoming.senderId == myId && incoming.receiverId == peerId) ||
                        (incoming.senderId == peerId && incoming.receiverId == myId)

                if (!isRelevant) {
                    return
                }

                if (incoming.senderId == myId) {
                    markLastOutgoingDelivered(incoming.id)
                } else if (!containsMessage(incoming.id)) {
                    _messages.value = _messages.value + incoming.toUi()
                }
            }

            override fun onFailure(failedSocket: WebSocket, t: Throwable, response: Response?) {
                if (webSocket === failedSocket) {
                    webSocket = null
                    _connectionStatus.value = "Disconnected"
                    scheduleReconnect()
                    return
                }

                connectSocketCandidate(myUserId, candidateIndex + 1)
            }

            override fun onClosed(closedSocket: WebSocket, code: Int, reason: String) {
                if (webSocket === closedSocket) {
                    webSocket = null
                    isConnectingSocket = false
                    _connectionStatus.value = "Disconnected"
                    scheduleReconnect()
                }
            }
        })
    }

    fun sendMessage(content: String) {
        val senderId = ChatSession.userId ?: dummyPhoneToId[ChatSession.phoneNumber] ?: return
        val receiverId = activePeerId ?: return
        if (content.isBlank()) {
            return
        }

        val localMessageId = UUID.randomUUID().toString()
        _messages.value = _messages.value + UiChatMessage(
            localId = localMessageId,
            senderId = senderId,
            content = content,
            timestamp = nowTime(),
            status = DeliveryStatus.SENT
        )

        val payload = gson.toJson(SendMessageRequestDto(senderId, receiverId, content))
        val sentOverSocket = webSocket?.send(payload) == true
        if (sentOverSocket) {
            return
        }

        val token = ChatSession.token
        if (!token.isNullOrBlank()) {
            viewModelScope.launch {
                runCatching {
                    BackendClient.messageApi.send(
                        SendMessageRequestDto(senderId, receiverId, content),
                        "Bearer $token"
                    )
                }.onSuccess { saved ->
                    replacePendingWithSaved(localMessageId, saved)
                }
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        val token = ChatSession.token ?: return
        val peerId = activePeerId ?: return

        _messages.value = _messages.value.filterNot { it.id == messageId }

        viewModelScope.launch {
            runCatching {
                BackendClient.messageApi.delete(messageId, "Bearer $token")
            }.onFailure {
                loadHistory(peerId)
            }
        }
    }

    override fun onCleared() {
        historySyncJob?.cancel()
        webSocket?.close(1000, "ViewModel cleared")
        webSocket = null
        _connectionStatus.value = "Disconnected"
        super.onCleared()
    }

    private fun MessageDto.toUi(): UiChatMessage = UiChatMessage(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = formatTime(createdAt),
        status = if (senderId == ChatSession.userId) DeliveryStatus.DELIVERED else DeliveryStatus.RECEIVED
    )

    private fun formatTime(iso: String): String {
        return runCatching {
            OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrDefault("--:--")
    }

    private fun nowTime(): String =
        OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    private fun markLastOutgoingDelivered(serverMessageId: Long) {
        val me = ChatSession.userId ?: dummyPhoneToId[ChatSession.phoneNumber] ?: return
        val list = _messages.value.toMutableList()
        val idx = list.indexOfLast { it.senderId == me }
        if (idx >= 0) {
            list[idx] = list[idx].copy(id = serverMessageId, status = DeliveryStatus.DELIVERED)
            _messages.value = list
        }
    }

    private fun replacePendingWithSaved(localId: String, saved: MessageDto) {
        val list = _messages.value.toMutableList()
        val idx = list.indexOfFirst { it.localId == localId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(id = saved.id, status = DeliveryStatus.DELIVERED)
            _messages.value = list
        }
    }

    private fun containsMessage(id: Long): Boolean = _messages.value.any { it.id == id }

    private fun scheduleReconnect() {
        val userId = reconnectUserId ?: return
        if (webSocket != null || isConnectingSocket) return

        viewModelScope.launch {
            delay(1500)
            if (webSocket == null && !isConnectingSocket) {
                connectSocket(userId)
            }
        }
    }

    private fun startHistorySync(peerId: Long) {
        historySyncJob?.cancel()
        historySyncJob = viewModelScope.launch {
            while (isActive) {
                loadHistory(peerId)
                delay(1500)
            }
        }
    }
}
