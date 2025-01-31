package com.example.thakkali.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@Composable
fun Disease(navController: NavController, imageUri: String?) {
    val uri = imageUri?.let { android.net.Uri.parse(it) }
    Column(
        modifier = Modifier.fillMaxSize()
            .background(DarkColors.background)
    ) {
        uri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier.size(200.dp)
            )
        }
    }
}