package com.example.thakkali.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(navController: NavController) {
    var query = remember { mutableStateOf("") }
    var response = remember { mutableStateOf("Ask me about agriculture!") }
    val isLoading = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Search",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AI Assistant",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                color = DarkColors.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkColors.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = query.value,
            onValueChange = { query.value = it },
            placeholder = { Text("Ask about farming...") },
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkColors.surface, RoundedCornerShape(12.dp)),
//            colors = TextFieldDefaults.textFieldColors(
//                textColor = DarkColors.onSurface,
//                backgroundColor = DarkColors.surface
//            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (query.value.isNotBlank()) {
                    isLoading.value = true
                    response.value = "Thinking..."
                    fetchAIResponse(query.value) { result ->
                        isLoading.value = false
                        response.value = result
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text("Ask", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkColors.surface, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(response.value, color = DarkColors.onSurface, fontSize = 18.sp)
        }

        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

// Placeholder function for AI response
fun fetchAIResponse(query: String, callback: (String) -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        callback("Here is some advice on $query...")
    }, 2000)
}
