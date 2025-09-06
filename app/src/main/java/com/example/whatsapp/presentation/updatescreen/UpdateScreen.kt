package com.example.whatsapp.presentation.updatescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun UpdateScreen(navHostController: NavHostController) {

    val scrollState = rememberScrollState()   // we are nt getting any value so use '=' rather 'by'

    val sampleStatus = listOf(
        StatusData(
            Image = R.drawable.disha_patani,
            name = "Disha",
            time = "10 min ago"
        ),
        StatusData(
            Image = R.drawable.hrithik_roshan,
            name = "Hrithik",
            time = "56 min ago"
        ),
        StatusData(
            Image = R.drawable.ajay_devgn,
            name = "Ajay",
            time = "2 hr ago"
        )
    )

    val sampleChannel = listOf(
        Channels(
            image = R.drawable.neat_roots,
            name = "Neat Roots",
            description = "Latest news on tech"
        ),
        Channels(
            image = R.drawable.neat_roots,
            name = "Neat Roots",
            description = "Latest news on tech"
        ),
        Channels(
            image = R.drawable.carryminati,
            name = "Carry Minaty",
            description = "More fun contents"
        ),
        Channels(
            image = R.drawable.whatsapp_icon,
            name = "WhatsApp",
            description = "Latest Updates on the app"
        ),
        Channels(
            image = R.drawable.meta,
            name = "Meta",
            description = "Leading technologies"
        ),
        Channels(
            image = R.drawable.rcb_logo,
            name = "Royal Challengers Bengaluru",
            description = "We are not juts RCBians , we are family"
        )
    )

    Scaffold(
        floatingActionButton = {

            FloatingActionButton(
                onClick = {},
                containerColor = colorResource(R.color.light_green),
                modifier = Modifier.size(65.dp),  // changes color for box
                contentColor = Color.White               // changes color for content
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_photo_camera_24),
                    contentDescription = null
                )
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
        topBar = { TopBar() }
    ) {
        // by default padding values are needed to be passed whenever we use Scaffold
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Text(
                text = "Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            MyStatus()

            sampleStatus.forEach { data ->

                StatusItem(statusData = data)

            }
            Spacer(modifier = Modifier.padding(16.dp))

            HorizontalDivider(color = Color.Gray)

            Text(
                text = "Channels",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Text(text = "stay updated on topics that matter to you. Find channels to follow below ")

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Find channels to follow")
            }
            Spacer(modifier = Modifier.height(16.dp))

            sampleChannel.forEach {
                ChannelItemDesign(channels = it)
            }

        }
    }
}
