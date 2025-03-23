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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun Search(navController: NavController) {
    val context = LocalContext.current
    val query = remember { mutableStateOf("") }
    val response = remember { mutableStateOf("Ask me about agriculture!") }
    val isLoading = remember { mutableStateOf(false) }
    val displayedText = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
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
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.search1),
                    contentDescription = "Search",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Assistant",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
                    color = DarkColors.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkColors.onSurface
                    )
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
                    Text(
                        displayedText.value,
                        color = DarkColors.onSurface,
                        fontSize = 16.sp
                    )
                    AsyncImage(
                        model = imageUrl.value,
                        contentDescription = "Disease Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    placeholder = { Text("Ask about farming...") },
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .background(DarkColors.surface)
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (query.value.isNotBlank()) {
                            imageUrl.value = ""
                            isLoading.value = true
                            response.value = "Thinking..."
                            fetchAIResponse(context, query.value) { result, image ->
                                isLoading.value = false
                                response.value = result
                                imageUrl.value = image
                            }
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .background(DarkColors.onSurface, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5AA16D),
                    ),
                    enabled = !isLoading.value
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text("Ask", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

fun fetchAIResponse(context: Context, query: String, callback: (String, String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://qb45f440-5000.inc1.devtunnels.ms/chat"
            val json = JSONObject().apply { put("query", query) }
            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            val responseBody = response.body?.string()

            responseBody?.let {
                val jsonObject = JSONObject(it)
                val textResponse = jsonObject.getString("response")
                val images = jsonObject.getString("images")

                withContext(Dispatchers.Main) {
                    callback(textResponse, images)
                }
            }
        } catch (e: Exception) {
            Log.e("AI", "Error fetching response", e)
            withContext(Dispatchers.Main) {
                callback("Error fetching response", "")
            }
        }
    }
}