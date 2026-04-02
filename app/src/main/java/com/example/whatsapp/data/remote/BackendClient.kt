package com.example.whatsapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BackendConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val messageApi: MessageApi by lazy { retrofit.create(MessageApi::class.java) }
    val contactApi: ContactApi by lazy { retrofit.create(ContactApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
}

