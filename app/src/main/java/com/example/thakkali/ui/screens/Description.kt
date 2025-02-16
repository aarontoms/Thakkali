package com.example.thakkali.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
fun Description(navController: NavController, disease: String) {
    val context = LocalContext.current
    var description = remember { mutableStateOf("Fetching description...") }

    LaunchedEffect(disease) {
        fetchDiseaseDescription(disease, description, context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        Text(
            text = disease,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkColors.onBackground),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = description.value,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textAlign = TextAlign.Start
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text(text = "Back", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

fun fetchDiseaseDescription(disease: String, description: MutableState<String>, context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null)
    val json = JSONObject().apply {
        put("username", username)
        put("disease", disease)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://qb45f440-5000.inc1.devtunnels.ms/descGenerate")
        .post(json)
        .addHeader("Content-Type", "application/json")
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Description", "Failed: ${e.message}")
            description.value = "Failed to fetch description"
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { responseBody ->
                try {
                    val jsonObject = JSONObject(responseBody)
                    val formattedText = buildString {
                        // Disease and Description
                        append("Disease: ${jsonObject.getString("Disease")}\n\n")
                        append("Description: ${jsonObject.getString("Description")}\n\n")

                        // Symptoms (JSONArray)
                        append("Symptoms:\n")
                        val symptoms = jsonObject.optJSONArray("Symptoms")
                        if (symptoms != null) {
                            for (i in 0 until symptoms.length()) {
                                append("- ${symptoms.getString(i)}\n")
                            }
                        } else {
                            append("- No symptoms listed.\n")
                        }

                        // Causes (JSONArray)
                        append("\nCauses:\n")
                        val causes = jsonObject.optJSONArray("Causes")
                        if (causes != null) {
                            for (i in 0 until causes.length()) {
                                append("- ${causes.getString(i)}\n")
                            }
                        } else {
                            append("- No causes listed.\n")
                        }

                        // Short Term Steps (JSONArray)
                        append("\nShort-term Steps:\n")
                        val shortTermSteps = jsonObject.optJSONArray("Short Term Steps")
                        if (shortTermSteps != null) {
                            for (i in 0 until shortTermSteps.length()) {
                                append("- ${shortTermSteps.getString(i)}\n")
                            }
                        } else {
                            append("- No short-term steps listed.\n")
                        }

                        // Long Term Steps (JSONArray)
                        append("\nLong-term Steps:\n")
                        val longTermSteps = jsonObject.optJSONArray("Long Term Steps")
                        if (longTermSteps != null) {
                            for (i in 0 until longTermSteps.length()) {
                                append("- ${longTermSteps.getString(i)}\n")
                            }
                        } else {
                            append("- No long-term steps listed.\n")
                        }
                    }
                    description.value = formattedText
                } catch (e: JSONException) {
                    Log.e("JSON Parsing", "Error parsing JSON: ${e.message}")
                    description.value = "Error loading description."
                }
            }
//            description.value = responseBody
        }
    })
}
