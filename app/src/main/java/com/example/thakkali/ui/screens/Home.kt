package com.example.thakkali.ui.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.thakkali.ui.theme.DarkColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.thakkali.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.net.URI

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkColors.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(top = 50.dp, start = 8.dp, bottom = 20.dp),

                ) {
                Image(
                    painter = painterResource(id = R.drawable.tomato),
                    contentDescription = "tomato",
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thakkali",
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    color = DarkColors.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))

//                IconButton(
//                    onClick = { navController.navigate("profile") }, modifier = Modifier.size(80.dp)
//                ) {
//                    Column(
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.profile),
//                            contentDescription = "Profile Icon",
//                            modifier = Modifier.size(35.dp)
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "Profile",
//                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
//                            color = DarkColors.onSurface
//                        )
//                    }
//                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val categories = listOf(
                    R.drawable.tomato2 to "Tomato",
                    R.drawable.mango to "Mango",
                    R.drawable.corn to "Corn",
                    R.drawable.history to "Placeholder",
                    R.drawable.history to "Placeholder"
                )

                categories.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { (imageRes, name) ->
                            Button(
                                onClick = {
                                    navController.navigate("capture?plantCategory=$name")
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = name,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkColors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

    }
}


fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
    val contentResolver = context.contentResolver
    val file = File(context.cacheDir, "upload.jpg")

    contentResolver.openInputStream(imageUri)?.use { inputStream ->
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", "image.jpg", requestBody).build()

    val request = Request.Builder().url("https://envs.sh").post(multipartBody).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Upload ENVS", "Failed: ${e.message}")
            callback(null)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val url = responseBody.trim()
                    Log.d("Upload ENVS", "Captured image to envs: $url")
                    callback(url)
                } else {
                    callback(null)
                }
            } else {
                Log.e("Upload ENVS", "Error: ${response.code}")
                callback(null)
            }
        }
    })
}