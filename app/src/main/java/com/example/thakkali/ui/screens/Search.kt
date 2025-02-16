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
import androidx.compose.foundation.text.handwriting.handwritingHandler
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
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect


fun fetchAIResponse(context: Context, query: String, callback: (String) -> Unit) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null)
    val json = JSONObject().apply {
        put("username", username)
        put("query", query)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://qb45f440-5000.inc1.devtunnels.ms/chat")
        .post(json)
        .addHeader("Content-Type", "application/json")
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Search", "Failed: ${e.message}")
            callback("Failed to fetch response")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseBody ->
                callback(responseBody)
            }
        }
    })
}

@Composable
fun Search(navController: NavController) {
    val context = LocalContext.current
    val query = remember { mutableStateOf("") }
    val response = remember { mutableStateOf("Ask me about agriculture!") }
    val isLoading = remember { mutableStateOf(false) }
    val displayedText = remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(response.value) {
        displayedText.value = ""
        response.value.forEachIndexed { index, char ->
            kotlinx.coroutines.delay(10)
            displayedText.value += char
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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
        }

        Column {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                reverseLayout = true
            ) {
                item {
                    Text(displayedText.value, color = DarkColors.onSurface, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = query.value,
                onValueChange = { query.value = it },
                placeholder = { Text("Ask about farming...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkColors.surface, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (query.value.isNotBlank()) {
                        isLoading.value = true
                        response.value = "Thinking..."
                        fetchAIResponse(context, query.value) { result ->
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
        }
    }
}