package com.example.thakkali.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@Composable
fun AppFooter(navController: NavController) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x00000000),
                        Color(0xC3000000)
                    )
                )
            ),
        containerColor = Color.Transparent
    ) {
        IconButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.size(100.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_empty),
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    "Home",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { navController.navigate("search") },
            modifier = Modifier.size(100.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Camera",
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    "Search",
                    color = Color.White,
                    fontSize = 12.sp
                )

            }
        }

        Spacer(modifier = Modifier.weight(1f))

//        IconButton(
//            onClick = { navController.navigate("history") },
//            modifier = Modifier.size(100.dp)
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.database),
//                    contentDescription = "History",
//                    modifier = Modifier.size(30.dp)
//                )
//                Text(
//                    "History",
//                    color = Color.White,
//                    fontSize = 12.sp
//                )
//            }
//        }
        //                IconButton(
//                    onClick = { navController.navigate("profile") }, modifier = Modifier.size(80.dp)
//                ) {
//                    Column(
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.profile),
//                            contentDescription = "Profile Icon",
//                            modifier = Modifier.size(35.dp)
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "Profile",
//                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
//                            color = DarkColors.onSurface
//                        )
//                    }
//                }
        IconButton(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.size(100.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    "Profile",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}