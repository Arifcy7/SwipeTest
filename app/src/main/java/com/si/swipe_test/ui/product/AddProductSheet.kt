package com.si.swipe_test.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    productName: String,
    productType: String,
    price: String,
    tax: String,
    imageUri: Uri?,
    isSubmitting: Boolean,
    onProductNameChange: (String) -> Unit,
    onProductTypeChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onImageUriChange: (Uri?) -> Unit,
    onAddProductClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val productTypes = listOf("Product", "Service")
    var dropdownExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> onImageUriChange(uri) }
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Product", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = productName,
                onValueChange = onProductNameChange,
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = !dropdownExpanded }) {
                OutlinedTextField(
                    value = productType,
                    onValueChange = {},
                    label = { Text("Product Type") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    productTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                onProductTypeChange(type)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                label = { Text("Selling Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tax,
                onValueChange = onTaxChange,
                label = { Text("Tax Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text(if (imageUri != null) "Change Image" else "Select Image")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddProductClick,
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Product")
                }
            }
        }
    }
}
