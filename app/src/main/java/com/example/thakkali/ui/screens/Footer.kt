package com.example.thakkali.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@Composable
fun AppFooter(navController: NavController) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = DarkColors.onTertiary
    ) {
        IconButton(onClick = { navController.navigate("camera") }) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Camera",
                tint = DarkColors.onSurface
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { navController.navigate("home") }) {
            Icon(
                painter = painterResource(id = R.drawable.home),
                contentDescription = "Home",
                tint = DarkColors.onSurface
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { navController.navigate("profile") }) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile",
                tint = DarkColors.onSurface
            )
        }
    }
}