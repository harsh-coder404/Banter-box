package com.example.whatsapp.presentation.userregistrationscreen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.whatsapp.R
import com.example.whatsapp.presentation.navigation.Routes
import com.example.whatsapp.presentation.viewmodel.AuthState
import com.example.whatsapp.presentation.viewmodel.PhoneAuthViewModel

@Composable
fun UserRegistrationScreen(
    navController: NavHostController,
    phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()
) {

    val authState by phoneAuthViewModel.authState.collectAsState()
    val context = LocalContext.current                        // taking the context of the screen
    val context1 = LocalContext.current
    val activity = context1 as Activity    // typecasting the context into Activity


    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedCountry by remember {
        mutableStateOf("Japan")
    }
    var countryCode by remember {
        mutableStateOf("+1")
    }
    var phoneNumber by remember {
        mutableStateOf("")
    }
    var otp by remember {
        mutableStateOf("")
    }
    var verificationId by remember {         // it will used to check whether the incoming otp was the same that was sent from Firebase not from any other source
        mutableStateOf<String?>(null)
    }

    val dummyPhones = remember { setOf("9890989098", "6262626262", "8787878787") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Enter your Phone Number",
            fontSize = 20.sp,
            color = colorResource(R.color.dark_green),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text(text = "Banterbox will need to verify your phone number")
            Spacer(modifier = Modifier.width(4.dp))

        }
        Text(text = "what's my number?", color = colorResource(R.color.dark_green))

        Spacer(modifier = Modifier.height(16.dp))

        Box {

            TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {

                Box(modifier = Modifier.width(230.dp)) {
                    Text(
                        text = selectedCountry,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "v",
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = colorResource(R.color.light_green),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

                listOf(
                    "India",
                    "USA",
                    "China",
                    "Canada"
                ).forEach { country ->   // provides each options

                    DropdownMenuItem(
                        text = { Text(text = country) },
                        onClick = {   // for each item of DDM what should be displayed as text and what action should be performed
                            selectedCountry = country
                            expanded = false
                        })
                }
            }
        }

        when (authState) {

            is AuthState.Ideal, is AuthState.Loading, is AuthState.CodeSent -> {

                if (authState is AuthState.CodeSent) {

                    verificationId = (authState as AuthState.CodeSent).verificationId
                }

                if (verificationId == null) {   // if verificationId didn't came

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        TextField(           // for country code
                            value = countryCode,
                            onValueChange = { countryCode = it },
                            modifier = Modifier.width(70.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = colorResource(R.color.light_green)
                            )
                        )
                        Spacer(Modifier.width(8.dp))

                        TextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            placeholder = { Text("Phone Number") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {

                            if (phoneNumber.isNotEmpty()) {   // to check if user has provided their number or not

                                if (phoneNumber in dummyPhones) {
                                    verificationId = "DUMMY"
                                    Toast.makeText(context, "Use OTP 0000", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val fullPhoneNumber = "$countryCode$phoneNumber"

                                phoneAuthViewModel.sendVerificationCode(fullPhoneNumber, activity)
                            } else {

                                Toast.makeText(
                                    context,
                                    "Please enter a valid Phone Number",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            colorResource(R.color.dark_green)
                        )

                    ) {

                        Text("Send OTP")
                    }

                    if (authState is AuthState.Loading) {

                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }

                } else {

                    // TODO OTP  Input Screen

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        "Enter OTP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.dark_green)
                    )

                    Spacer(Modifier.height(8.dp))

                    TextField(
                        value = otp,
                        onValueChange = { otp = it },
                        placeholder = { Text("OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {

                            if (phoneNumber in dummyPhones && otp == "0000") {
                                phoneAuthViewModel.loginDummyAccount(phoneNumber, context)
                            } else if (otp.isNotEmpty() && verificationId != null) {   // if otp is entered

                                phoneAuthViewModel.verifyCode(otp, context)

                            } else {   // if not entered or left null

                                Toast.makeText(
                                    context,
                                    "Please enter a valid OTP",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(
                            colorResource(R.color.dark_green)
                        )
                    ) {

                        Text("verify OTP")
                    }

                    if (authState is AuthState.Loading){

                        Spacer(Modifier.height(16.dp))

                        CircularProgressIndicator()
                    }
                }
            }

            is AuthState.Success -> {

                // using log to check if successful or not
                Log.d("PhoneAuth", "LoginSuccessful")

                phoneAuthViewModel.resetAuthState()  // reset if success

                navController.navigate(Routes.HomeScreen){

                    popUpTo<Routes.UserRegistrationScreen>{    // pop Up from navBackStackEntry
                        inclusive = true
                    }
                }
            }

            is AuthState.Error -> {

                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }

        }
    }
}