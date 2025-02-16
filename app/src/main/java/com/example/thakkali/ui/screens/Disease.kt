package com.example.thakkali.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.thakkali.ui.theme.DarkColors
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.thakkali.ml.CornH5Inception
import com.example.thakkali.ml.Mango
import com.example.thakkali.ml.TomatoH5Inception
import com.example.thakkali.ml.TomatoKerasInception
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder


@SuppressLint("DefaultLocale")
@Composable
fun Disease(navController: NavController, imageUri: String?, plantCategory: String) {


    val uri = imageUri?.let { Uri.parse(it) }
    Log.e("Disease", "Image URI: $uri")
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null) ?: "Anonymous"

    var diseaseResult = remember { mutableStateOf("Detection result will appear here") }
    var loading = remember { mutableStateOf(false) }
    var diseaseName = remember { mutableStateOf("") }
    var confidenceMeter = remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(uri) {
        uri?.let { sendUriToMongo(it.toString(), username) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            uri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                uri?.let {
                    loading.value = true
                    CoroutineScope(Dispatchers.IO).launch {
                        detectDisease(
                            context,
                            it,
                            diseaseResult,
                            loading,
                            diseaseName,
                            plantCategory,
                            confidenceMeter
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            enabled = !loading.value && diseaseName.value.isEmpty()
        ) {
            if (!loading.value) {
                if (diseaseName.value.isNotEmpty()) {
                    Text(
                        text = "Disease Detected",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        text = "Detect Disease",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            } else {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))

        if (diseaseName.value.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(Color.DarkGray, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diseaseName.value,
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.2f%%", confidenceMeter.doubleValue * 100),
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .shadow(12.dp, CircleShape)
                                .background(
                                    when {
                                        confidenceMeter.doubleValue >= 0.85 -> Color.Green
                                        confidenceMeter.doubleValue in 0.70..0.84 -> Color(
                                            0xFFFFA500
                                        )

                                        else -> Color.Red
                                    }, CircleShape
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (diseaseName.value != "Not Detected") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { navController.navigate("description?diseaseName=${diseaseName.value}") }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap to View More Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "Try changing the angle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text(
                                text = "Retake Image",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

fun sendUriToMongo(imageUri: String, username: String) {
    val json =
        """{"username": "$username", "uri": "$imageUri"}""".toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://qb45f440-5000.inc1.devtunnels.ms/upload")
        .post(json)
        .addHeader("Content-Type", "application/json")
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Upload", "Failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            Log.d("Upload", "Success: ${response.body?.string()}")
        }
    })
}

suspend fun detectDisease(
    context: Context,
    imageUri: Uri,
    diseaseResult: MutableState<String>,
    loading: MutableState<Boolean>,
    diseaseName: MutableState<String>,
    plantCategory: String,
    confidenceValue: MutableState<Double>
) {
    Log.d("DiseaseDetection", "Detecting disease... $imageUri")
    diseaseName.value = ""

    val bitmap = loadImageFromUrl(imageUri.toString())
    val byteBuffer = bitmap?.let { convertBitmapToByteBuffer(it) }

//    val h5model = TomatoH5Inception.newInstance(context)
//    val kerasmodel = TomatoKerasInception.newInstance(context)
//
//    val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 299, 299, 3), DataType.FLOAT32)
//    byteBuffer?.let { inputFeature.loadBuffer(it) }
//
//    val outputs1 = h5model.process(inputFeature)
//    val outputs2 = kerasmodel.process(inputFeature)
//
//    h5model.close()
//    kerasmodel.close()
//
//    val (predictedClass1, confidence1) = getDiseaseLabel(outputs1.outputFeature0AsTensorBuffer.floatArray)
//    val (predictedClass2, confidence2) = getDiseaseLabel(outputs2.outputFeature0AsTensorBuffer.floatArray)
//
//    Log.d("DiseaseDetection", "Model h5: $predictedClass1, Confidence: $confidence1")
//    Log.d("DiseaseDetection", "Model keras: $predictedClass2, Confidence: $confidence2")
//
//    withContext(Dispatchers.Main) {
//        if (confidence1 < 0.7 || confidence2 < 0.7) {
//            diseaseResult.value = "Unable to detect disease. Please retake the picture."
//        } else {
//            diseaseResult.value = "Detected disease: $predictedClass1 with confidence $confidence1"
//            diseaseName.value = predictedClass1
//        }
//        loading.value = false
//    }


    val model: Any = when (plantCategory) {
        "Tomato" -> TomatoH5Inception.newInstance(context)
        "Mango" -> Mango.newInstance(context)
        "Corn" -> {
            Log.e("Balls Man", "Corn Model")
            CornH5Inception.newInstance(context)
        }
        else -> TomatoKerasInception.newInstance(context)
    }
    val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 299, 299, 3), DataType.FLOAT32)
    byteBuffer?.let { inputFeature.loadBuffer(it) }

    val output = when (model) {
        is TomatoH5Inception -> model.process(inputFeature).outputFeature0AsTensorBuffer
        is Mango -> model.process(inputFeature).outputFeature0AsTensorBuffer
        is CornH5Inception -> model.process(inputFeature).outputFeature0AsTensorBuffer
        is TomatoKerasInception -> model.process(inputFeature).outputFeature0AsTensorBuffer
        else -> throw IllegalStateException("Unexpected model type")
    }
    val (predictedClass, confidence) = getDiseaseLabel(output.floatArray, plantCategory)

    Log.d("DiseaseDetection", "Model: $predictedClass, Confidence, $confidence for $plantCategory")
    withContext(Dispatchers.Main) {
        if (confidence <= 0.70) {
            diseaseResult.value = "Unable to detect disease. Please retake the picture."
            diseaseName.value = "Not Detected"
            confidenceValue.value = confidence.toDouble()
        } else if (confidence > 0.70 && confidence < 0.85) {
            diseaseResult.value = "Detected disease: $predictedClass with confidence $confidence."
            diseaseName.value = predictedClass
            confidenceValue.value = confidence.toDouble()
        } else {
            diseaseResult.value = "Detected disease: $predictedClass with confidence $confidence"
            diseaseName.value = predictedClass
            confidenceValue.value = confidence.toDouble()
        }
        loading.value = false
    }
}

suspend fun loadImageFromUrl(imageUrl: String): Bitmap? {
    return try {
        val url = URL(imageUrl)
        withContext(Dispatchers.IO) {
            val inputStream: InputStream = url.openStream()
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val inputSize = 299
    val inputChannels = 3
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * inputChannels)
    byteBuffer.order(ByteOrder.nativeOrder())
    val intValues = IntArray(inputSize * inputSize)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    scaledBitmap.getPixels(
        intValues,
        0,
        scaledBitmap.width,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height
    )
    var pixelIndex = 0
    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val pixelValue = intValues[pixelIndex++]
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))
        }
    }
    return byteBuffer
}

fun getDiseaseLabel(predictions: FloatArray, plantCategory: String): Pair<String, Float> {

    val tomatoLabels =
        listOf("Bacterial Spot", "Early Blight", "Late Blight", "Septoria Leaf Spot", "Healthy")
    val mangoLabels = listOf(
        "Anthracnose",
        "Bacterial Canker",
        "Cutting Weevil",
        "Die Back",
        "Gall Midge",
        "Healthy",
        "Powdery Mildew",
        "Sooty Mould"
    )
    val cornLabels = listOf("Common Rust", "Gray Leaf Spot", "Healthy", "Northern Leaf Blight")
    val labels = when (plantCategory) {
        "Tomato" -> tomatoLabels
        "Mango" -> mangoLabels
        "Corn" -> cornLabels
        else -> tomatoLabels
    }

    val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: -1
    val confidence = if (maxIndex != -1) predictions[maxIndex] else 0f
    return labels.getOrElse(maxIndex) { "Unknown" } to confidence
}

