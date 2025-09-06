package com.example.whatsapp.presentation.callscreen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whatsapp.R

@Composable
@Preview(showSystemUi = true)
fun FavoritesSection() {

    val sampleFavorites = listOf(
        FavoriteContact(image = R.drawable.rashmika, name = "Rashmika"),
        FavoriteContact(image = R.drawable.akshay_kumar, name = "Akshay"),
        FavoriteContact(image = R.drawable.mrbeast, name = "MrBeast"),
        FavoriteContact(image = R.drawable.tripti_dimri, name = "Tripti"),
        FavoriteContact(image = R.drawable.bhuvan_bam, name = "Bhuvan"),
        FavoriteContact(image = R.drawable.sharadha_kapoor, name = "Shradha")
    )

    Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {

        Text(
            "Favorites",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            fontSize = 20.sp
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())) {

            sampleFavorites.forEach {
                FavoritesItem(it)
            }
        }
    }
}

data class FavoriteContact(
    val image: Int,
    val name: String
)