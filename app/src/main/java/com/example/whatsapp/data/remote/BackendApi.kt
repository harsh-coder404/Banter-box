package com.example.whatsapp.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

object BackendConfig {
    const val BASE_URL = "http://10.0.2.2:8081/"
    private const val WS_PATH = "/ws/chat"

    fun wsCandidates(): List<String> {
        val parsed = BASE_URL.toHttpUrlOrNull()
        if (parsed == null) {
            return listOf("ws://10.0.2.2:8081$WS_PATH")
        }

        val scheme = if (parsed.isHttps) "wss" else "ws"
        val port = parsed.port
        val candidates = linkedSetOf(
            "$scheme://${parsed.host}:$port$WS_PATH",
            "$scheme://10.0.2.2:$port$WS_PATH",
            "$scheme://10.0.3.2:$port$WS_PATH",
            "$scheme://localhost:$port$WS_PATH",
            "$scheme://127.0.0.1:$port$WS_PATH"
        )
        return candidates.toList()
    }
}

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class AuthResponseDto(
    val userId: Long,
    val name: String,
    val phoneNumber: String,
    val token: String
)

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val content: String,
    val createdAt: String
)

data class SendMessageRequestDto(
    val senderId: Long,
    val receiverId: Long,
    val content: String
)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponseDto
}

interface MessageApi {
    @POST("api/messages")
    suspend fun send(
        @Body body: SendMessageRequestDto,
        @Header("Authorization") authorization: String
    ): MessageDto

    @GET("api/messages/{otherUserId}")
    suspend fun history(
        @Path("otherUserId") otherUserId: Long,
        @Header("Authorization") authorization: String
    ): List<MessageDto>

    @DELETE("api/messages/{messageId}")
    suspend fun delete(
        @Path("messageId") messageId: Long,
        @Header("Authorization") authorization: String
    )
}

interface ContactApi {
    @GET("api/contacts")
    suspend fun contacts(@Header("Authorization") authorization: String): List<ContactDto>

    @POST("api/contacts")
    suspend fun addContact(
        @Body body: AddContactRequestDto,
        @Header("Authorization") authorization: String
    ): ContactDto
}

interface UserApi {
    @GET("api/users/by-phone/{phoneNumber}")
    suspend fun userByPhone(
        @Path("phoneNumber") phoneNumber: String,
        @Header("Authorization") authorization: String
    ): ContactDto
}

data class ContactDto(
    val id: Long,
    val name: String,
    val phoneNumber: String
)

data class AddContactRequestDto(
    val contactPhoneNumber: String
)
