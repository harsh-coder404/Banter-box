package com.example.whatsapp.model

data class PhoneAuthUser(   // data to send to the backend if auth succesfull
    val userId: String = "",
    val phoneNumber:String = "",
    val name: String = "",
    val status: String = "",
    val profileImage:String? = null
)
