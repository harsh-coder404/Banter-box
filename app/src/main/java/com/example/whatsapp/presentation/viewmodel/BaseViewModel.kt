package com.example.whatsapp.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.whatsapp.data.remote.AddContactRequestDto
import com.example.whatsapp.data.remote.BackendClient
import com.example.whatsapp.data.remote.ContactDto
import com.example.whatsapp.data.session.ChatSession
import com.example.whatsapp.presentation.chat_box.ChatListModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class BaseViewModel : ViewModel() {

    private val dummyChats = listOf(
        ChatListModel(name = "Golu", phoneNumber = "9890989098", time = "09:20", message = "Tap to chat"),
        ChatListModel(name = "Monu", phoneNumber = "6262626262", time = "10:45", message = "Tap to chat"),
        ChatListModel(name = "Sonu", phoneNumber = "8787878787", time = "11:10", message = "Tap to chat")
    )

    private fun dummyChatsForCurrentUser(): List<ChatListModel> {
        val me = ChatSession.phoneNumber
        return if (me.isNullOrBlank()) dummyChats else dummyChats.filter { it.phoneNumber != me }
    }

    fun searchUserByPhoneNumber(phoneNumber: String, callback: (ChatListModel?) -> Unit) {
        val token = ChatSession.token
        if (token.isNullOrBlank()) {
            Log.e("BaseViewModel", "Session token missing")
            callback(null)
            return
        }

        viewModelScope.launch {
            runCatching {
                BackendClient.userApi.userByPhone(phoneNumber, "Bearer $token")
            }.onSuccess { user ->
                callback(
                    ChatListModel(
                        name = user.name,
                        phoneNumber = user.phoneNumber,
                        time = "--:--",
                        message = "Tap to chat"
                    )
                )
            }.onFailure {
                callback(null)
            }
        }

    }

    private val _chatList = MutableStateFlow<List<ChatListModel>>(emptyList())
    val chatList = _chatList.asStateFlow()

    init {
        LoadChatData()
    }

    fun refreshChats() {
        LoadChatData()
    }

    private fun LoadChatData() {
        val token = ChatSession.token
        if (token.isNullOrBlank()) {
            _chatList.value = dummyChatsForCurrentUser()
            return
        }

        viewModelScope.launch {
            val authHeader = "Bearer $token"

            runCatching {
                BackendClient.contactApi.contacts(authHeader)
            }.onSuccess { contacts ->
                val mePhone = ChatSession.phoneNumber

                val byPhone = linkedMapOf<String, ContactDto>()
                contacts.forEach { byPhone[it.phoneNumber] = it }

                // Ensure always-visible dummy users are hydrated from backend if present.
                dummyChatsForCurrentUser().forEach { dummy ->
                    val phone = dummy.phoneNumber ?: return@forEach
                    if (phone == mePhone || byPhone.containsKey(phone)) return@forEach
                    runCatching {
                        BackendClient.userApi.userByPhone(phone, authHeader)
                    }.onSuccess { user ->
                        byPhone[user.phoneNumber] = user
                    }
                }

                val rows = byPhone.values.map { contact ->
                    val history = runCatching {
                        BackendClient.messageApi.history(contact.id, authHeader)
                    }.getOrDefault(emptyList())

                    val last = history.lastOrNull()
                    val lastMessage = last?.content ?: "Tap to chat"
                    val lastTime = last?.createdAt?.let(::formatTime) ?: "--:--"

                    val dummyName = dummyChats.firstOrNull { it.phoneNumber == contact.phoneNumber }?.name
                    ChatListModel(
                        name = if (contact.name.isBlank()) dummyName else contact.name,
                        phoneNumber = contact.phoneNumber,
                        time = lastTime,
                        message = lastMessage
                    )
                }

                val fallback = dummyChatsForCurrentUser()
                val merged = (rows + fallback).distinctBy { it.phoneNumber }
                _chatList.value = if (merged.isEmpty()) fallback else merged
            }.onFailure {
                _chatList.value = dummyChatsForCurrentUser()
            }
        }
    }

    private fun formatTime(iso: String): String {
        return runCatching {
            OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrDefault("--:--")
    }

    fun addChat(newChat: ChatListModel) {
        val token = ChatSession.token
        val phone = newChat.phoneNumber

        if (token.isNullOrBlank() || phone.isNullOrBlank()) {
            Log.e("BaseViewModel", "Cannot add chat: missing token or phone")
            return
        }

        viewModelScope.launch {
            runCatching {
                BackendClient.contactApi.addContact(
                    AddContactRequestDto(contactPhoneNumber = phone),
                    "Bearer $token"
                )
            }.onSuccess {
                refreshChats()
            }.onFailure { exception ->
                Log.e("BaseViewModel", "Failed to add chat: ${exception.message}")
            }
        }
    }


    private fun decodeBase64toBitmap(base64Image: String): Bitmap?{

        return try{

            val decodedByte = Base64.decode(base64Image, Base64.DEFAULT)

            BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.size)

        }catch (e : IOException){

            null
        }

    }

    fun base64toBitmap(base64String: String): Bitmap?{

        return try {

            val decodedByte = Base64.decode(base64String, Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedByte)

            BitmapFactory.decodeStream(inputStream)

        }catch (e : IOException){
            null
        }
    }

}