package com.example.thakkali.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


@Composable
fun ShopProfile(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", "") ?: ""
    val savedUserId = sharedPreferences.getString("userid", null)
    var lat = remember { mutableStateOf("") }
    var lon = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val url = "${AppState.backendUrl}/fetchShopProfile"
                    val json = JSONObject().apply { put("username", username) }.toString()
                    val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
                    val request = Request.Builder().url(url).post(requestBody).build()
                    val client = OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build()
                    client.newCall(request).execute()
                }
                response.body?.string()?.let {
                    val json = JSONObject(it)
                    lat.value = json.optString("lat", "")
                    lon.value = json.optString("lon", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Shop Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Username: $username", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lat.value,
            onValueChange = { lat.value = it },
            label = { Text("Latitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lon.value,
            onValueChange = { lon.value = it },
            label = { Text("Longitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val url = "${AppState.backendUrl}/addShop"
                        val json = JSONObject().apply {
                            put("username", username)
                            put("userid", savedUserId)
                            put("lat", lat.value)
                            put("lon", lon.value)
                        }.toString()
                        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
                        val request = Request.Builder().url(url).post(requestBody).build()
                        OkHttpClient().newCall(request).execute()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            Text("Save Changes")
        }
    }
}
