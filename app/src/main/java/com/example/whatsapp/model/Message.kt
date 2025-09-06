package com.example.whatsapp.model

// creating a model through which msg will be sent or received
data class Message(
    val senderPhoneNumber: String = "",
    val message: String = "",
    val timeStamp: Long = 0L   // we will get time as ms so make it Long
)
