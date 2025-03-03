package com.example.thakkali.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thakkali.AppState
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

@SuppressLint("MutableCollectionMutableState")
@Composable
fun Dashboard(navController: NavController) {
    var stockList = remember { mutableStateOf(mutableListOf<Stock>()) }
    var showDialog = remember { mutableStateOf(false) }
    var editingStock = remember { mutableStateOf<Stock?>(null) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog.value = true },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Stock", tint = Color.White)
            }
        },
        bottomBar = {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { /* Navigate to Dashboard */ },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Dashboard",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text("Dashboard", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { navController.navigate("shopProfile") },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text("Profile", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Shop Inventory",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (stockList.value.isEmpty()) {
                Text("No stock available.", modifier = Modifier.padding(16.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    stockList.value.forEach { stock ->
                        StockItem(
                            stock = stock,
                            onIncrease = {
                                stockList.value = stockList.value.map {
                                    if (it.id == stock.id) it.copy(quantity = it.quantity + 1) else it
                                }.toMutableList()
                            },
                            onDecrease = {
                                if (stock.quantity > 0) {
                                    stockList.value = stockList.value.map {
                                        if (it.id == stock.id) it.copy(quantity = it.quantity - 1) else it
                                    }.toMutableList()
                                }
                            },
                            onEdit = { editingStock.value = stock }
                        )
                    }
                }

                editingStock.value?.let { it ->
                    EditStockDialog(
                        stock = it,
                        onDismiss = { editingStock.value = null },
                        onConfirm = { updatedStock ->
                            stockList.value = stockList.value.map {
                                if (it.id == updatedStock.id) updatedStock else it
                            }.toMutableList()
                            editingStock.value = null
                            sendStockUpdate(context, stockList.value)
                        },
                        onDelete = { deletedStock ->
                            stockList.value = stockList.value.filter { it.id != deletedStock.id }.toMutableList()
                            editingStock.value = null
                            sendStockUpdate(context, stockList.value)
                        }
                    )
                }
            }
        }
    }

    if (showDialog.value) {
        AddStockDialog(
            onAddStock = { newStock ->
                stockList.value = (stockList.value + newStock).toMutableList()
                showDialog.value = false
                sendStockUpdate(context, stockList.value)
            },
            onDismiss = { showDialog.value = false }
        )
    }
}

@Composable
fun StockItem(
    stock: Stock,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stock.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "₹${stock.price} | ${if (stock.organic) "Organic" else "Inorganic"}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease",
                        tint = Color.Red
                    )
                }
                Text(
                    "${stock.quantity}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = Color.Green)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddStockDialog(onAddStock: (Stock) -> Unit, onDismiss: () -> Unit) {
    var name = remember { mutableStateOf("") }
    var quantity = remember { mutableStateOf("") }
    var price = remember { mutableStateOf("") }
    var isOrganic = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Stock") },
        text = {
            Column {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Stock Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = { quantity.value = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = price.value,
                    onValueChange = { price.value = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Organic:" )
                    Spacer(Modifier.width(16.dp))
                    Switch(
                        checked = isOrganic.value,
                        onCheckedChange = { isOrganic.value = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotBlank() && quantity.value.isNotBlank() && price.value.isNotBlank()) {
                        onAddStock(
                            Stock(
                                UUID.randomUUID().toString(),
                                name.value,
                                quantity.value.toInt(),
                                price.value.toInt(),
                                isOrganic.value
                            )
                        )
                    }
                }
            ) {
                Text("Add Stock")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditStockDialog(
    stock: Stock,
    onDismiss: () -> Unit,
    onConfirm: (Stock) -> Unit,
    onDelete: (Stock) -> Unit
) {
    var name = remember { mutableStateOf(stock.name) }
    var quantity = remember { mutableStateOf(stock.quantity.toString()) }
    var price = remember { mutableStateOf(stock.price.toString()) }
    var isOrganic = remember { mutableStateOf(stock.organic) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stock") },
        text = {
            Column {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = { quantity.value = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = price.value,
                    onValueChange = { price.value = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Organic:")
                    Spacer(Modifier.width(16.dp))
                    Switch(
                        checked = isOrganic.value,
                        onCheckedChange = { isOrganic.value = it }
                    )
                }
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        onConfirm(
                            stock.copy(
                                name = name.value,
                                quantity = quantity.value.toInt(),
                                price = price.value.toInt(),
                                organic = isOrganic.value
                            )
                        )
                    }
                ) {
                    Text("Save")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onDelete(stock)
                    },
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }

    )
}

data class StockRequest(
    @SerializedName("userid") val userid: String,
    @SerializedName("inventory") val inventory: List<Stock>
)

fun sendStockUpdate(context: Context, stockList: List<Stock>) {
    val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val savedUserId = sharedPreferences.getString("userid", null)?: ""

    val jsonBody = Gson().toJson(StockRequest(savedUserId, stockList))
    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url("${AppState.backendUrl}/updateShop")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API", "Failed to update shop: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            Log.d("API", "Response: ${response.body?.string()}")
        }
    })
}

data class Stock(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val organic: Boolean
)
