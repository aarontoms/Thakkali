package com.example.thakkali.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(mapView) {
        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        googleMap.addMarker(
                            MarkerOptions().position(userLatLng).title("Your Location")
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))

                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addressList = geocoder.getFromLocationName(
                            "Agriculture Shop",
                            5,
                            it.latitude - 0.1,
                            it.longitude - 0.1,
                            it.latitude + 0.1,
                            it.longitude + 0.1
                        )
                        if (!addressList.isNullOrEmpty()) {
                            for (address in addressList) {
                                val latLng = LatLng(address.latitude, address.longitude)
                                googleMap.addMarker(
                                    MarkerOptions().position(latLng).title("Agriculture Shop")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
