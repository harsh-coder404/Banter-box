package com.example.whatsapp.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Routes {

    @Serializable
    data object SplashScreen:Routes()

    @Serializable
    data object WelcomeScreen:Routes()

    @Serializable
    data object UserRegistrationScreen:Routes()

    @Serializable
    data object HomeScreen:Routes()

    @Serializable
    data object UpdateScreen: Routes()

    @Serializable
    data object CommunitiesScreen:Routes()

    @Serializable
    data object CallScreen : Routes()

    @Serializable
    data object UserProfileSetScreen: Routes()

    @Serializable
    data object SettingScreen: Routes()

    @Serializable
    data object ChatScreen: Routes(){

        const val route = "chat_screen/{phoneNumber}"
        fun createRoutes(phoneNumber: String) = "chat_screen/$phoneNumber"  // creates a unique routes for each different phoneNumbers for chatting Screen

    }
}