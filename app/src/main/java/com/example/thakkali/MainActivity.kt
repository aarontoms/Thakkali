package com.example.thakkali

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thakkali.ui.screens.AppFooter
import com.example.thakkali.ui.screens.Capture
import com.example.thakkali.ui.screens.Description
import com.example.thakkali.ui.screens.Disease
import com.example.thakkali.ui.screens.History
import com.example.thakkali.ui.screens.Home
import com.example.thakkali.ui.screens.Login
import com.example.thakkali.ui.screens.Profile
import com.example.thakkali.ui.screens.Search
import com.example.thakkali.ui.screens.Signup
import com.example.thakkali.ui.screens.SplashScreen
import com.example.thakkali.ui.screens.Welcome
import com.example.thakkali.ui.theme.DarkColors
import com.example.thakkali.ui.theme.ThakkaliTheme

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
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val savedUserId = sharedPreferences.getString("userid", null)
    val startDestination = if (savedUserId == null) "welcome" else "search"
    Log.e("AppNavigator", "Userid: $savedUserId")

    val navController = rememberNavController()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("splash") { SplashScreen(navController) }
            composable("welcome") { Welcome(navController) }
            composable("login") { Login(navController) }
            composable("signup") { Signup(navController) }
            composable("home") { PageWithFooter(navController) { Home(navController) } }
            composable("history") { PageWithFooter(navController) { History(navController) } }
            composable("search") { PageWithFooter(navController) { Search(navController) } }
            composable("profile") { PageWithFooter(navController) { Profile(navController) } }
            composable("capture?plantCategory={plantCategory}") { backStackEntry ->
                val plantCategory = backStackEntry.arguments?.getString("plantCategory")
                Capture(navController, plantCategory)
            }
            composable("disease?imageUri={imageUri}&plantCategory={plantCategory}") { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri")
                val plantCategory = backStackEntry.arguments?.getString("plantCategory") ?: ""
                Disease(navController, imageUri, plantCategory)
            }
            composable("description?diseaseName={diseaseName}") { backStackEntry ->
                val diseaseName = backStackEntry.arguments?.getString("diseaseName") ?: ""
                Description(navController, diseaseName)
            }
        }
    }
}

@Composable
fun PageWithFooter(navController: NavController, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        AppFooter(navController)
    }
}

@Composable
fun SwipeNavigation(navController: NavController) {
    val pagerState =
        androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 }, initialPage = 1)

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