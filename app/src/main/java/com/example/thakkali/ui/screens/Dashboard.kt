package com.example.thakkali.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

@SuppressLint("MutableCollectionMutableState")
@Composable
fun Dashboard() {
    var stockList = remember { mutableStateOf(mutableListOf<Stock>()) }
    var showDialog = remember { mutableStateOf(false) }
    var editingStock = remember { mutableStateOf<Stock?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog.value = true },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Stock", tint = Color.White)
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

                editingStock.value?.let {
                    EditStockDialog(
                        stock = it,
                        onDismiss = { editingStock.value = null },
                        onConfirm = { updatedStock ->
                            stockList.value = stockList.value.map {
                                if (it.id == updatedStock.id) updatedStock else it
                            }.toMutableList()
                            editingStock.value = null
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
                    "₹${stock.price} | ${if (stock.organic) "Organic" else "Non-Organic"}",
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
    var isOrganic = remember { mutableStateOf("Non-Organic") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Stock") },
        text = {
            Column {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Stock Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = { quantity.value = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price.value,
                    onValueChange = { price.value = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    listOf("Organic", "Non-Organic").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = { isOrganic.value = it })
                    }
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
                                isOrganic.value == "Organic"
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
    onConfirm: (Stock) -> Unit
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
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = { quantity.value = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price.value,
                    onValueChange = { price.value = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Organic:")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isOrganic.value,
                        onCheckedChange = { isOrganic.value = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    stock.copy(
                        name = name.value,
                        quantity = quantity.value.toInt(),
                        price = price.value.toInt(),
                        organic = isOrganic.value
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


data class Stock(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val organic: Boolean
)
