package com.example.thakkali.ui.screens

import android.content.Context
import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.thakkali.R
import com.example.thakkali.ui.theme.AlegrayaFontFamily
import com.example.thakkali.ui.theme.DarkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun L(
    navController: NavHostController
) {
    val isLoading = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = FocusRequester()
    var errorMessage by remember { mutableStateOf("") }
    val passwordFocusRequester = FocusRequester()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Surface(
        color = Color(0xFF253334),
        modifier = Modifier.fillMaxSize()
            .clickable { focusManager.clearFocus() },

    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg1),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .align(Alignment.BottomCenter)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.tomato),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 200.dp)
                        .height(100.dp)
                        .align(Alignment.Start)
                        .offset(x = (-20).dp)
                )

                Text(
                    text = "Sign In",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = AlegrayaFontFamily,
                        fontWeight = FontWeight(500),
                        color = Color.White
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

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
                    onClick = {
                        isLoading.value = true
                        handleLogin(username, password) { success, message, userid ->
                            isLoading.value = false
                            println("Login result: $userid")
                            if (success) {
                                val sharedPreferences = context.getSharedPreferences(
                                    "user_session",
                                    Context.MODE_PRIVATE
                                )
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.putString("userid", userid)
                                editor.apply()
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                errorMessage = message
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkColors.onPrimary,
                    )
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(color = DarkColors.onSurface)
                    } else {
                        Text(
                            text = "Login",
                            color = DarkColors.onSurface,
                            modifier = Modifier.padding(4.dp),
                            style = TextStyle(fontSize = 20.sp)
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(12.dp)
                    )
                }
            }
        }

    }

}
