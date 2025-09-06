package com.example.whatsapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // preparing instances of firebase so to access firebase properties in our project
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth{   // function for authentication

        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase{

        return FirebaseDatabase.getInstance()
    }
}