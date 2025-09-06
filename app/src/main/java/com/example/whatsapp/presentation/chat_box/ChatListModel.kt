package com.example.whatsapp.presentation.chat_box

data class ChatListModel(   // making all nullable becoz firebase expects incoming data to be nullable

    val name: String? = null,
    val phoneNumber: String? = null,
    val image: Int? = null,
    val userId: String? = null,
    val time: String? = null,
    val message: String? = null,
    val profileImage: String? = null

) {
    constructor() : this(null, null, null, null, null, null, null)
}