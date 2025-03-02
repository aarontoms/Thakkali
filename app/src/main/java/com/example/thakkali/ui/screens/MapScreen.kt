package com.example.thakkali.ui.screens

import android.Manifest
import android.annotation.SuppressLint
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

import android.location.Location
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.thakkali.AppState
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors
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


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var googleMapState = remember { mutableStateOf<GoogleMap?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var shopList = remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var userLatLng = remember { mutableStateOf<LatLng?>(null) }
    var clickedShop = remember { mutableStateOf<JSONObject?>(null) }


    LaunchedEffect(mapView) {
        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            googleMapState.value = googleMap
            if (locationPermissionState.status.isGranted) {
                fetchLocationAndMark(
                    fusedLocationClient,
                    googleMap,
                    context,
                    clickedShop
                ) { shops, LatLng ->
                    shopList.value = shops
                    userLatLng.value = LatLng
                }
            } else {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            if (shopList.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Fetching shops...",
                            color = DarkColors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
            shopList.value.forEach { shop ->
                Log.e("MapScreen", "Shop: $shop")
                val isSelected = shop == clickedShop.value
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLatLng.value!!.latitude, userLatLng.value!!.longitude,
                    shop.getDouble("lat"), shop.getDouble("lon"), results
                )
                shop.put("distance", results[0] / 1000)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .animateContentSize()
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clickable {
                            clickedShop.value = shop
                            googleMapState.value?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(shop.getDouble("lat"), shop.getDouble("lon")),
                                    14f
                                )
                            )
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.background(if (isSelected) Color(0xFFC9C9C9) else Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shop.getString("username"),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFF424242) else Color(
                                            0xFF212121
                                        ),
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Text(
                                    text = "${
                                        String.format(
                                            "%.2f",
                                            shop.getDouble("distance")
                                        )
                                    } km",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF616161) else Color(0xFF757575),
                                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            for (index in 0 until shop.getJSONArray("inventory").length()) {
                                val item = shop.getJSONArray("inventory").getJSONObject(index)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row {
                                            Text(
                                                text = item.getString("itemname"),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Color(0xFF424242) else Color(
                                                    0xFF616161
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(20.dp))
                                            if (item.getBoolean("organic")) {
                                                Box(
                                                    modifier = Modifier
                                                        .border(
                                                            2.dp,
                                                            Color(0xFF388E3C),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(2.dp)
                                                ) {
                                                    Text(
                                                        text = "Organic",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF388E3C)
                                                    )
                                                }
                                            }

                                        }
                                        Text(
                                            text = "Stock: ${item.getInt("quantity")}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF424242) else Color(
                                                0xFF616161
                                            ),
                                        )
                                    }
                                    Text(
                                        text = "₹${item.getInt("price")}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF212121) else Color(
                                            0xFF424242
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun fetchLocationAndMark(
    fusedLocationClient: FusedLocationProviderClient,
    googleMap: GoogleMap,
    context: Context,
    clickedShop: MutableState<JSONObject?>,
    onShopSelected: (List<JSONObject>, LatLng) -> Unit
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
                        val requestBody =
                            json.toString().toRequestBody("application/json".toMediaType())

                        val request = Request.Builder()
                            .url("${AppState.backendUrl}/getShops")
                            .post(requestBody)
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string()
                        if (!response.isSuccessful || responseBody.isNullOrEmpty()) return@launch

                        val shops = JSONArray(responseBody)
                        val shopList = List(shops.length()) { i -> shops.getJSONObject(i) }
                        Log.e("BALLS", "balls $shopList")
                        withContext(Dispatchers.Main) {
                            for (shop in shopList) {
                                val latLng = LatLng(shop.getDouble("lat"), shop.getDouble("lon"))
                                val bitmap = BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.store
                                )
                                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
                                val bitmapDescriptor =
                                    BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                                val marker = googleMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(shop.getString("username"))
                                        .icon(bitmapDescriptor)
                                )
                                marker?.tag = shop
                            }
                            onShopSelected(shopList, userLatLng)

                            googleMap.setOnMarkerClickListener { marker ->
                                Log.e("MapScreen", "Marker clicked ${marker.tag}")
                                (marker.tag as? JSONObject)?.let { shop ->
                                    clickedShop.value = shop
                                }
                                false
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
