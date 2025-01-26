package com.example.thakkali.ui.screens

import android.util.Log
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Home(navController: NavController) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkColors.onTertiary),
        ) {
            Column(
                modifier = Modifier
                .padding(top = 52.dp, start = 16.dp)
            ){

                Text(
                    text = "Thakkali",
                    style = TextStyle(fontSize = 28.sp),
                    color = DarkColors.onSurface,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }

        Button(
            onClick = { navController.navigate("history") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("History")
        }

        Button(
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    Log.d("Home", "Camera permission granted brooo")
                } else {
                    // If permission is not granted, request permission
                    cameraPermissionState.launchPermissionRequest()
                }
//                navController.navigate("camera")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text("📷")
        }

    }
}
