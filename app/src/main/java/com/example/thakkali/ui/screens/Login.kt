package com.example.thakkali.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.thakkali.R
import com.example.thakkali.ui.theme.DarkColors

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkColors.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.tomato),
            contentDescription = "App Logo",
            modifier = Modifier.size(160.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController) {
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    val passwordFocusRequester = FocusRequester()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box() {
        Image(
            painter = painterResource(id = R.drawable.tom2),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable { focusManager.clearFocus() },
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 56.dp, start = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tomato),
                    contentDescription = "tomato",
                    modifier = Modifier.size(140.dp)
                )
            }
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.Center

            ) {
                Text(
                    "Welcome back,", style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        "Login", style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 20.dp, start = 16.dp)

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .focusRequester(usernameFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = DarkColors.onPrimary,
                            focusedLabelColor = DarkColors.onPrimary,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .focusRequester(passwordFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = DarkColors.onPrimary,
                            focusedLabelColor = DarkColors.onPrimary,
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkColors.onPrimary,
                        )
                    ) {
                        Text(
                            text = "Login",
                            color = DarkColors.onSurface,
                            modifier = Modifier.padding(4.dp),
                            style = TextStyle(fontSize = 20.sp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { navController.navigate("forgot") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(onClick = { navController.navigate("signup") }) {
                            Text(
                                text = "New User? Sign Up",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
