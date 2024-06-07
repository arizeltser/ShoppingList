package com.example.myshoppinglist

import android.Manifest
import android.content.Context
import android.graphics.drawable.Icon
import android.net.InetAddresses
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

data class ShoppingItem(
    val id:Int, 
    var name:String, 
    var quantity:Int, 
    var isEditing:Boolean = false,
    var address:String = ""
)

@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
) {
    var sItems by remember{ mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // have access
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            }
            else {
                // ask for permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION

                ) || ActivityCompat.shouldShowRequestPermissionRationale (
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationaleRequired) {
                    Toast.makeText(context, "Location permission is required for this feature to work",
                        Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(context, "Location permission is required. PLease enable in settings",
                        Toast.LENGTH_LONG).show()
                }


            }
        })
    
    
    
    
    

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {showDialog = true},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Add Item")
        }
        LazyColumn(
            modifier= Modifier
                .fillMaxSize()
                .padding(16.dp),

        ){
            items(sItems) {
                item ->
                if(item.isEditing){
                    ShoppingItemEditor(
                        item = item,
                        onSaveClick = {
                            editedName, editedQuantity, editedAddress ->
                            sItems = sItems.map{it.copy(isEditing = false)}
                            val editedItem = sItems.find{it.id == item.id}
                            editedItem?.let {
                                it.name = editedName
                                it.quantity = editedQuantity
                                it.address = address
                            }
                        }
                    )
                }
                else {
                    ShoppingListItem(
                        item = item,
                        onEditClick = {
                            sItems = sItems.map { it.copy(isEditing = it.id==item.id) }
                        },
                        onDeleteClick = {
                            sItems = sItems-item
                        }
                    )
                }
            }
        }
    }

    if(showDialog){
        AlertDialog(
            onDismissRequest = {showDialog = false},
            confirmButton = {
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Button(onClick = {
                                    if(itemName.isNotBlank()) {
                                        val newItem = ShoppingItem(sItems.size+1,itemName,itemQuantity.toInt(),address=address)
                                        sItems = sItems + newItem
                                        showDialog = false
                                        itemName = ""
                                    }
                                }) {
                                    Text(text = "Add")
                                }
                                Button(onClick = {showDialog = false}) {
                                    Text(text = "Cancel")
                                }
                            }
            },
            title = { Text("")},
            text = {
                Column (){
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {itemName = it},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = {itemQuantity = it},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    
                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)) {
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationscreen") {
                                this.launchSingleTop
                            }
                        }
                        else {
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }) {
                        Text(text = "Address")
                    }
                }
            }
        )
    }
}

@Composable
fun ShoppingListItem(item:ShoppingItem, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val customTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 20.sp,
        color = Color.Black,
        fontWeight = FontWeight.Bold

    )


    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color(0XFF018786)),
                shape = RoundedCornerShape(20)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Column(modifier = Modifier
            .weight(1f)
            .padding(8.dp)) {
            Row {
                Text(text = item.name.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(8.dp), style = customTextStyle)
                Text(text = "Qty: ${item.quantity.toString()}", modifier = Modifier.padding(8.dp), style = customTextStyle)
            }
            Row(modifier=Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.LocationOn,contentDescription = null)
                Text(text = item.address)
            }
        }
        
        
        
        
        Row(modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }

    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onSaveClick: (String,Int,String) -> Unit) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var isEditing by remember { mutableStateOf(item.isEditing) }

    var editedAddress by remember { mutableStateOf(item.address) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        Column (){
            BasicTextField(value = editedName, onValueChange = {editedName = it}, singleLine = true, modifier = Modifier
                .wrapContentSize()
                .padding(8.dp))
            BasicTextField(value = editedQuantity, onValueChange = {editedQuantity = it}, singleLine = true,modifier = Modifier
                .wrapContentSize()
                .padding(8.dp))
            BasicTextField(value = editedAddress, onValueChange = {editedAddress = it}, singleLine = true,modifier = Modifier
                .wrapContentSize()
                .padding(8.dp))
        }
        Button(onClick =
        {
            isEditing = false
            onSaveClick(editedName,editedQuantity.toIntOrNull() ?: 1,editedAddress)
        }, modifier = Modifier.padding(8.dp)) {
            Text(text = "Save")
        }
    }
}