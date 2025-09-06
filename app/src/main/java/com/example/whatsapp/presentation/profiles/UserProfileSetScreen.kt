package com.example.whatsapp.presentation.profiles

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.whatsapp.R
import com.example.whatsapp.presentation.navigation.Routes
import com.example.whatsapp.presentation.viewmodel.PhoneAuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun UserProfileSetScreen(
    phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel(),
    navHostController: NavHostController
) {

    var name by remember {
        mutableStateOf("")
    }
    var status by remember {
        mutableStateOf("")
    }
    var profileImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var bitmapImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val firebaseAuth = Firebase.auth
    val phoneNumber = firebaseAuth.currentUser?.phoneNumber
        ?: ""   //  taking phone number from firebase for current user and if null then return empty string
    val userId = firebaseAuth.currentUser?.uid ?: ""

    val context = LocalContext.current

    val imagePickerLauncher =
        rememberLauncherForActivityResult(   // picker to get image from gallery
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->         // converting image as String to store in firebase
                // uri is the image and we have to convert it into String
                profileImageUri = uri

                uri?.let {

                    // to get some permission thus checking version
                    bitmapImage =
                        if (Build.VERSION.SDK_INT < 28) {   //  if version < 28 ( conversion process)

                            @Suppress("DEPRECATION")
                            android.provider.MediaStore.Images.Media.getBitmap(
                                context.contentResolver,
                                it
                            )

                        } else {  // if version >= 28 (conversion process)

                            val source = ImageDecoder.createSource(context.contentResolver, it)
                            ImageDecoder.decodeBitmap(source)
                        }

                }

            }
        )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .border(2.dp, color = Color.Gray, shape = CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }) {

            // if image is in bitmap form
            if (bitmapImage != null) {  // to check if bitmap is not null

                Image(
                    bitmap = bitmapImage!!.asImageBitmap(),   // bypassing null safety // converting image to bitmap
                    contentDescription = null,
                    Modifier
                        .fillMaxSize()
                        .clip(
                            CircleShape
                        ), contentScale = ContentScale.Crop
                )
            }

            // if profile image is not null
            else if (profileImageUri != null) {

                Image(
                    painter = rememberImagePainter(profileImageUri),  // Coil - to show image that we obtain from any server or URL in app
                    contentDescription = null,
                    Modifier
                        .fillMaxSize()
                        .clip(
                            CircleShape
                        ), contentScale = ContentScale.Crop
                )

            } else {

                Image(
                    painter = painterResource(R.drawable.img_1),
                    contentDescription = null,
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = phoneNumber)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text("Name")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.light_green)
            )
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = status,
            onValueChange = { status = it },
            label = {
                Text("Your Status")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.light_green)
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                phoneAuthViewModel.savedUserProfile(userId, name, status, bitmapImage)
                navHostController.navigate(Routes.HomeScreen)
            },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.light_green))
        ) {

            Text("Save")
        }
    }
}


