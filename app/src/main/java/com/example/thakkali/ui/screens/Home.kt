package com.example.thakkali.ui.screens

import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.thakkali.R
import java.io.File
import java.net.URI

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(navController: NavController) {
    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val imageUri = remember { mutableStateOf<Uri?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
    ) {
//        imageUri.value?.let { uri ->
//            Image(
//                painter = rememberAsyncImagePainter(uri),
//                contentDescription = "Selected Image",
//                modifier = Modifier.size(200.dp)
//            )
//        }

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

            IconButton(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.size(80.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
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

        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    Log.d("Cameraa", "Image captured successfully")
                    imageUri.value?.let {
                        Log.d("Image URL", "Image URI: $it")
                        navController.navigate("disease?imageUri=${Uri.encode(it.toString())}")
                    }
                }
            }
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
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 80.dp)
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(R.drawable.camera),
                contentDescription = "Background Image",
                modifier = Modifier
                    .heightIn(max = 80.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(60.dp))


        val galleryLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    Log.d("Gallery", "Image selected successfully")
                    imageUri.value = uri
                    navController.navigate("disease?imageUri=${Uri.encode(it.toString())}")
                }
            }
        Button(onClick = { galleryLauncher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.weight(1f))

        AppFooter(navController)
    }
}
