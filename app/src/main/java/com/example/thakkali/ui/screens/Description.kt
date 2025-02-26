package com.example.thakkali.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.thakkali.ui.theme.DarkColors
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONArray

@Composable
fun Description(navController: NavController, disease: String, plantCategory: String) {
    val context = LocalContext.current
    var description = remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(disease) {
        fetchDiseaseDescription(disease, description, context, plantCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(16.dp)
            .padding(top = 48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$disease of $plantCategory",
            style = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkColors.onBackground
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val imageUrl = description.value?.get("images") as? String
        imageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = "Disease Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        description.value?.let { data ->
            InfoContainer(
                title = "Disease",
                content = data["Disease"] as? String ?: "Unable to retrieve"
            )
            InfoContainer(
                title = "Description",
                content = data["Description"] as? String ?: "Unable to retrieve"
            )
            InfoContainer(title = "Symptoms", list = data["Symptoms"] as? List<String>)
            InfoContainer(title = "Causes", list = data["Causes"] as? List<String>)
            InfoContainer(
                title = "Short-term Steps",
                list = data["Short Term Steps"] as? List<String>
            )
            InfoContainer(
                title = "Long-term Steps",
                list = data["Long Term Steps"] as? List<String>
            )
            InfoContainer(
                title = "Medications",
                list = data["Medications"] as? List<String>,
                navController = navController
            )
        } ?: Text(text = "Fetching description...", color = Color.White)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(
                text = "Back",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun InfoContainer(
    title: String,
    content: String? = null,
    list: List<String>? = null,
    navController: NavController? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .then(if (title == "Medications") Modifier.clickable { navController?.navigate("map") } else Modifier)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Yellow,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        content?.let {
            Text(text = it, style = TextStyle(fontSize = 16.sp, color = Color.White))
        }

        list?.forEach { item ->
            Text(
                text = if (title == "Medications") item else "- $item",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = if (title == "Medications") FontWeight.Bold else FontWeight.Normal,
                    color = if (title == "Medications") Color.Cyan else Color.White,
                    textDecoration = if (title == "Medications") TextDecoration.Underline else null
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

fun fetchDiseaseDescription(
    disease: String,
    description: MutableState<Map<String, Any>?>,
    context: Context,
    plantCategory: String
) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null)
    val json = JSONObject().apply {
        put("username", username)
        put("disease", disease)
        put("species", plantCategory)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://qb45f440-5000.inc1.devtunnels.ms/descGenerate")
        .post(json)
        .addHeader("Content-Type", "application/json")
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Description", "Failed: ${e.message}")
            description.value = mapOf("Error" to "Failed to fetch description")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseBody ->
                try {
                    val jsonObject = JSONObject(responseBody)
                    val data = mutableMapOf<String, Any>()
                    jsonObject.keys().forEach { key ->
                        data[key] = when (val value = jsonObject.opt(key)) {
                            is JSONArray -> List(value.length()) { value.getString(it) }
                            else -> value.toString()
                        }
                    }
                    description.value = data
                } catch (e: JSONException) {
                    Log.e("JSON Parsing", "Error parsing JSON: ${e.message}")
                    description.value = mapOf("Error" to "Error loading description.")
                }
            }
        }
    })
}
