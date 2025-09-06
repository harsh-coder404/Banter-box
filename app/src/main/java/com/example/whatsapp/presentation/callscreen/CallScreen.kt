package com.example.whatsapp.presentation.callscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.whatsapp.R
import com.example.whatsapp.presentation.bottomnavigation.BottomNavigation
import com.example.whatsapp.presentation.navigation.Routes

@Composable
fun CallScreen(navHostController: NavHostController) {

    var isSearching by remember {
        mutableStateOf(false)
    }

    var search by remember {
        mutableStateOf("")
    }

    var showMenu by remember {
        mutableStateOf(false)
    }

    val sampleCalls = listOf(
        Call(image = R.drawable.bhuvan_bam,
            name = "Bhuvan",
            time = "36 min ago",
            isMissed = true
        ),
        Call(image = R.drawable.akshay_kumar,
            name = "Akshay",
            time = "23 hr ago",
            isMissed = false
        ),
        Call(image = R.drawable.disha_patani,
            name = "Disha",
            time = "Yesterday 10:46AM",
            isMissed = true
        ),
        Call(image = R.drawable.ajay_devgn,
            name = "Ajay",
            time = "Yesterday 8:00 AM",
            isMissed = true
        ),
        Call(image = R.drawable.hrithik_roshan,
            name = "Hrithik",
            time = "Tuesday 7:18 PM",
            isMissed = false
        ),
        Call(image = R.drawable.tripti_dimri,
            name = "Tripti",
            time = "Monday 6:45 PM",
            isMissed = false
        ),
        Call(image = R.drawable.shahrukh_khan,
            name = "Shahrukh",
            time = "Monday 9:12 AM",
            isMissed = true
        ),
        Call(image = R.drawable.salman_khan,
            name = "Salman",
            time = "Sunday 12:09 PM",
            isMissed = true
        ),

    )

    Scaffold(

        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {

                Column {

                    Row {

                        if (isSearching) {

                            TextField(
                                value = search, onValueChange = {
                                    search = it
                                },
                                placeholder = { Text(text = "Search") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.padding(start = 12.dp), singleLine = true
                            )
                        } else {
                            Text(
                                text = "Calls",
                                fontSize = 28.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 12.dp, top = 16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))   // can be used to move the below item far away
                        if (isSearching) {

                            IconButton(onClick = {
                                isSearching = false
                                search = ""
                            }) {

                                Icon(
                                    painter = painterResource(R.drawable.cross),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        } else {

                            IconButton(onClick = { isSearching = true }) {

                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { showMenu = true }) {

                                Icon(
                                    painter = painterResource(R.drawable.more),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }) {

                                    DropdownMenuItem(
                                        text = { Text(text = "Settings") },
                                        onClick = { showMenu = false })
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        },

        bottomBar = {
            BottomNavigation(navHostController, selectedItem = 0, onClick = { index ->

                when(index){

                    0 -> {navHostController.navigate(Routes.HomeScreen)}
                    1-> {navHostController.navigate(Routes.UpdateScreen)}
                    2-> {navHostController.navigate(Routes.CommunitiesScreen)}
                    3-> {navHostController.navigate(Routes.CallScreen)}

                }
            })
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {},
                containerColor = colorResource(R.color.light_green),
                modifier = Modifier.size(65.dp),  // changes color for box
                contentColor = Color.White               // changes color for content
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_call),
                    contentDescription = null
                )
            }
        }

    ) {

        Column(modifier = Modifier.padding(it)) {

            Spacer(Modifier.height(16.dp))

            FavoritesSection()

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.light_green)
                )
            ) {
                Text(
                    "Start a new Call",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Recent Calls",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn {

                items(sampleCalls) { it ->

                    CallItemDesign(it)

                }
            }
        }
    }
}