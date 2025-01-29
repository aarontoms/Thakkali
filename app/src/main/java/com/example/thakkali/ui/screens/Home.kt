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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.thakkali.R
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(navController: NavController) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("Home", "Image captured successfully")
            imageUri.value?.let {
                Log.d("Image URL", "Image URI: $it")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkColors.onTertiary)
                .padding(top = 52.dp, start = 8.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
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
        }
        Button(
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Thakkali")
                    }

                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        imageUri.value = uri
                        launcher.launch(uri)
                    } else {
                        Log.e("Error", "Failed to create MediaStore entry")
                    }
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
//                .fillMaxWidth()
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

        Button(
            onClick = { navController.navigate("history") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "History",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AppFooter(navController)
    }
}
