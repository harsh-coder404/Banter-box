package com.example.whatsapp.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.whatsapp.model.PhoneAuthUser
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _authState =
        MutableStateFlow<AuthState>(AuthState.Ideal)  // to represent authentication state

    // ideal state means do nothing (empty initially)
    val authState = _authState.asStateFlow()

    // creating node (node - place where all user data will be stored) , like creating a folder
    private val userRef = database.reference.child("users")

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {

        _authState.value = AuthState.Loading   // Authentication state should change to loading form ideal once this fun start running

        val option = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // for otp (passcode for backend)
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(id, token)

                Log.d("PhoneAuth", "onCodeSent triggered. verification ID: $id")
                _authState.value = AuthState.CodeSent(verificationId = id)
            }

            // for backend if verification completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                signingWithCredential(credential, context = activity)
            }

            override fun onVerificationFailed(exception: FirebaseException) {

                Log.d("PhoneAuth", "Verification failed: ${exception.message}")
                _authState.value = AuthState.Error(exception.message ?: "Verification failed")
            }
        }

        // to send otp (thing needed to send otp)
        val phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)  // timeout for otp entry
            .setActivity(activity)                         // activity in which otp needed to be sent
            .setCallbacks(option)                         // error handling
            .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)   // to send otp

    }

    //  checks if the user correctly signed in if yes then he can enter
    private fun signingWithCredential(credential: PhoneAuthCredential, context: Context) {

        _authState.value = AuthState.Loading

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val user = firebaseAuth.currentUser
                val phoneAuthUser = PhoneAuthUser(
                    userId = user?.uid ?: "",
                    phoneNumber = user?.phoneNumber ?: ""
                )

                // if user successfully signed in then below fun runs and user auth screen will not appear again and state is loaded as success
                markUserAsSignedIn(context)
                _authState.value = AuthState.Success(phoneAuthUser)

                fetchUserProfile(user?.uid ?: "") // if old user then fetch data
            } else {
                // if sign in failed
                _authState.value = AuthState.Error(task.exception?.message ?: "Sign-in failed")
            }
        }
    }

    // this fun is used to save a flag in the app to check whether the user is signed in or not
    private fun markUserAsSignedIn(context: Context) {   // context is used to access any data from the app

        // sharedPreference is a storage inside the app where data is stored in value pairs (eg - here "app_prefs" is a file to store sharedPreference)
        val sharedPreference = context.getSharedPreferences(
            "app_prefs",
            Context.MODE_PRIVATE
        )  // context is private for the app, other apps cant access it
        sharedPreference.edit().putBoolean("isSignedIn", true).apply()

        // edit - used to update the data of sharedPreference ,  isSignedIn (key) tells that whether the user is signed in or not
    }


    private fun fetchUserProfile(userId: String) {  // userId is a key to fetch user details

//        userRef - creates an instance
        val userRef =
            userRef.child(userId)   // accessing user's details from database through userRef instance

        userRef.get().addOnSuccessListener { snapshot ->   // .get() - to fetch the data

            if (snapshot.exists()) {   // if the data exists

                val userProfile =
                    snapshot.getValue(PhoneAuthUser::class.java)  // data fetched from database is converted to PhoneAuthUser format (if converted - that data will be stored in user profile)

                if (userProfile != null) {  // checks if user profile is successfully fetched and converted ( if oot null then success )

                    _authState.value = AuthState.Success(userProfile)
                }
            }
        }.addOnFailureListener {  // if error occurred

            _authState.value = AuthState.Error("Failed to fetch user profile")
        }
    }

    // to verify otp
    fun verifyCode(otp: String, context: Context) {

        val currentAuthState = _authState.value  // holds reference of _authState.value

        // to give user a message that either ID is wrong or the process haven't started (code sent not done)
        if (currentAuthState !is AuthState.CodeSent || currentAuthState.verificationId.isEmpty()) {

            Log.e("PhoneAuth", "Attempting to verify OTP without a valid verification ID")

            _authState.value = AuthState.Error("Verification not started or invalid ID")

            return
        }

        // if everything is fine, then by using this 'PhoneAuthProvider.getCredential' it create a credential using otp and id to tell the backend that whether the otp is correct or not
        val credential = PhoneAuthProvider.getCredential(currentAuthState.verificationId, otp)

        signingWithCredential(
            credential,
            context
        )  // this line send the credentials to backend for verification

    }

    // takes user,s name , status and image and store it in firebase cloud
    fun savedUserProfile(userId: String, name: String, status: String, profileImage: Bitmap?) {

        val database =
            FirebaseDatabase.getInstance().reference  // creating database instance to access its properties

        val encodedImage = profileImage?.let {
            convertBitmapToBase64(it)           // we will convert image to string using bitmap and store in database
        }

        val userProfile =
            PhoneAuthUser(   // making a basket to put all the user's data from firebase
                userId = userId,
                name = name,
                status = status,
                phoneNumber = Firebase.auth.currentUser?.phoneNumber ?: "",
                profileImage = encodedImage
            )
        // using specific userId to access user's data from db
        database.child("users").child(userId).setValue(userProfile)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {   // we will convert image to string using bitmap

        val byteArrayOutputStream = ByteArrayOutputStream()   // to convert image to byte form

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)  // compressing image to jpeg format with certain quality and putting in byteArrayOutputStream

        val byteArray = byteArrayOutputStream.toByteArray()    // convert to byteArray

        return Base64.encodeToString(byteArray, Base64.DEFAULT)    // converting byteArray to base64 String
    //         We use Base64 (standard method) to represent binary data in text format to send image in APIs

    }

    // reset Auth state whenever user sign out
    fun resetAuthState(){

        _authState.value = AuthState.Ideal
    }

//   to sign out or resetting
    fun signOut(activity: Activity){

        firebaseAuth.signOut()    // using this we are logging out the user from the firebase

        val sharedPreferences = activity.getSharedPreferences("app_prefs", Activity.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isSigned",false).apply()

    //  setting the flag value false
    }

}

sealed class AuthState {

    object Ideal : AuthState()  // ideal state means do nothing (empty)
    object Loading : AuthState()

    data class CodeSent(val verificationId: String) : AuthState()
    data class Success(val user: PhoneAuthUser) :
        AuthState()     // data to send to the firebase if auth successful

    data class Error(val message: String) : AuthState()
}