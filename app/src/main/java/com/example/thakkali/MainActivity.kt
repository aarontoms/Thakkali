package com.example.thakkali

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thakkali.ui.screens.Disease
import com.example.thakkali.ui.screens.History
import com.example.thakkali.ui.screens.Home
import com.example.thakkali.ui.screens.Login
import com.example.thakkali.ui.screens.Profile
import com.example.thakkali.ui.screens.Search
import com.example.thakkali.ui.screens.Signup
import com.example.thakkali.ui.screens.SplashScreen
import com.example.thakkali.ui.theme.DarkColors
import com.example.thakkali.ui.theme.ThakkaliTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThakkaliTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize().background(DarkColors.background)) {
        NavHost(navController = navController, startDestination = "home") {
            composable("splash") { SplashScreen(navController) }
            composable("login") { Login(navController) }
            composable("signup") { Signup(navController) }
            composable("home") {
                Home(navController)
            }
            composable("history") { History(navController) }
            composable("search") { Search(navController) }
            composable("profile") { Profile(navController) }
            composable("disease?imageUri={imageUri}") { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri")
                Disease(navController, imageUri)
            }
        }
    }
}

@Composable
fun SwipeNavigation(navController: NavController) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 }, initialPage = 1)

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> Search(navController)
            1 -> Home(navController)
            2 -> Profile(navController)
        }
    }
}