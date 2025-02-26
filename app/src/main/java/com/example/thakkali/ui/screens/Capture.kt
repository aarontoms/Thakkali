package com.example.thakkali.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Capture(navController: NavController, plantCategory: String?) {

    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val isLoading = remember { mutableStateOf(false) }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.d("Camera", "Image captured successfully")
                imageUri.value?.let {
                    Log.d("Image URL", "Image URI: $it")
                    isLoading.value = true
//                    uploadImage(context, it) { uploadedUrl ->
//                        isLoading.value = false
//                        if (uploadedUrl != null) {
//                            Handler(Looper.getMainLooper()).post {
//                                navController.navigate("disease?imageUri=${Uri.encode(uploadedUrl)}&plantCategory=$plantCategory")
//                            }
//                        } else {
//                            Log.e("Gallery", "Upload failed, not navigating")
//                        }
//                    }
                    Handler(Looper.getMainLooper()).post {
                        navController.navigate("disease?imageUri=${Uri.encode(it.toString())}&plantCategory=$plantCategory")
                    }
                }
            }
        }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Log.d("Gallery", "Image selected successfully")
                imageUri.value = uri
                isLoading.value = true
//                uploadImage(context, uri) { uploadedUrl ->
//                    isLoading.value = false
//                    if (uploadedUrl != null) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            navController.navigate("disease?imageUri=${Uri.encode(uploadedUrl)}&plantCategory=$plantCategory")
//                        }
//                    } else {
//                        Log.e("Gallery", "Upload failed, not navigating")
//                    }
//                }
                Handler(Looper.getMainLooper()).post {
                    Log.e("Gallery BAlls", "Image URI: $it")
                    navController.navigate("disease?imageUri=${Uri.encode(it.toString())}")
                }
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkColors.background)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B354B))
                    .padding(top = 50.dp, start = 20.dp, bottom = 20.dp),

                ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
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

                IconButton(
                    onClick = { navController.navigate("profile") }, modifier = Modifier.size(80.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user1),
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(35.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profile",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                            color = DarkColors.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            Text(
                text = "Capture ${plantCategory}",
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkColors.onBackground
                ),
                modifier = Modifier.padding(16.dp, top = 56.dp)
            )

            Spacer(modifier = Modifier.weight(0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            val contentValues = ContentValues().apply {
                                put(
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    "Thakkali_${System.currentTimeMillis()}.jpg"
                                )
                                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                put(
                                    MediaStore.Images.Media.RELATIVE_PATH,
                                    "${Environment.DIRECTORY_PICTURES}/Thakkali"
                                )
                            }

                            val uri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                            )
                            if (uri != null) {
                                imageUri.value = uri
                                cameraLauncher.launch(uri)
                            } else {
                                Log.e("Error", "Failed to create MediaStore entry")
                            }
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B354B),
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.camera2),
                            contentDescription = "Camera",
                            modifier = Modifier
                                .heightIn(max = 80.dp)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Camera",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                            color = DarkColors.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }


                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B354B),
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.gallery2),
                            contentDescription = "Upload Image",
                            modifier = Modifier
                                .heightIn(max = 80.dp)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Upload Image",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

        }
        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}