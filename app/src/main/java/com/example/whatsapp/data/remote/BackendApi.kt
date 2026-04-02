package com.example.whatsapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

object BackendConfig {
    const val BASE_URL = "http://10.0.2.2:8081/"
    const val WS_URL = "ws://10.0.2.2:8081/ws/chat"
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





