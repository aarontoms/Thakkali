package com.example.thakkali.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.thakkali.AppState
import com.example.thakkali.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var selectedShop = remember { mutableStateOf<JSONObject?>(null) }
    var isSheetVisible =  remember { mutableStateOf(false) }

    LaunchedEffect(mapView) {
        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            if (locationPermissionState.status.isGranted) {
                fetchLocationAndMark(fusedLocationClient, googleMap, context) { shop ->
                    selectedShop.value = shop
                    isSheetVisible.value = true
                }
            } else {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            selectedShop.let { shop ->
                Column(Modifier.padding(16.dp)) {
                    shop.value?.let { shopData ->
                        Text(text = shopData.getString("username"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    shop.value?.let { shopData ->
                        LazyColumn {
                            val inventory = shopData.getJSONArray("inventory")
                            items(inventory.length()) { index ->
                                val item = inventory.getJSONObject(index)
                                Text("${item.getString("itemname")}: ${item.getInt("price")} (Qty: ${item.getInt("quantity")})")
                            }
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun fetchLocationAndMark(
    fusedLocationClient: FusedLocationProviderClient,
    googleMap: GoogleMap,
    context: Context,
    onShopSelected: (JSONObject) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(MarkerOptions().position(userLatLng).title("Your Location"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build()

                        val json = JSONObject().apply {
                            put("lat", userLatLng.latitude)
                            put("lon", userLatLng.longitude)
                        }
                        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

                        val request = Request.Builder()
                            .url("${AppState.backendUrl}/getShops")
                            .post(requestBody)
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string()
                        if (!response.isSuccessful || responseBody.isNullOrEmpty()) return@launch

                        val shops = JSONArray(responseBody)
                        withContext(Dispatchers.Main) {
                            for (i in 0 until shops.length()) {
                                val shop = shops.getJSONObject(i)
                                val latLng = LatLng(shop.getDouble("lat"), shop.getDouble("lon"))
                                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.store)
                                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
                                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                                val marker = googleMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(shop.getString("username"))
                                        .icon(bitmapDescriptor)
                                )
                                marker?.tag = shop
                            }

                            googleMap.setOnMarkerClickListener { marker ->
                                Log.e("MapScreen", "Marker clicked")
                                (marker.tag as? JSONObject)?.let(onShopSelected)
                                true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MapScreen", "Failed to fetch shops", e)
                    }
                }
            }
        }
    }
}
