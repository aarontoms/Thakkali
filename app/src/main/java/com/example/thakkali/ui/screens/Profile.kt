package com.example.thakkali.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@Composable
fun Profile(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, DarkColors.onSurface, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "User Name",
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            color = DarkColors.onSurface
        )

        Text(
            text = "user@example.com",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            color = DarkColors.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileButton("Edit Profile") { /* Navigate to edit profile */ }
            ProfileButton("Settings") { /* Navigate to settings */ }
            ProfileButton("Log Out", isDestructive = true) { /* Handle logout */ }
        }
    }
}

@Composable
fun ProfileButton(text: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) Color.Red else DarkColors.onSurface
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}