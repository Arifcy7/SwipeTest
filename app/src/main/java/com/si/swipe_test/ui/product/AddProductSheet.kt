package com.si.swipe_test.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.si.swipe_test.R
import com.si.swipe_test.data.ProductFormData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    formData: ProductFormData,
    isSubmitting: Boolean,
    onFormChange: (ProductFormData) -> Unit,
    onAddProductClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add New Product",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            ImagePicker(
                imageUri = formData.imageUri,
                onImageUriChange = { onFormChange(formData.copy(imageUri = it)) }
            )

            ProductDetailsForm(
                productName = formData.productName,
                productType = formData.productType,
                price = formData.price,
                tax = formData.tax,
                onProductNameChange = { onFormChange(formData.copy(productName = it)) },
                onProductTypeChange = { onFormChange(formData.copy(productType = it)) },
                onPriceChange = { onFormChange(formData.copy(price = it)) },
                onTaxChange = { onFormChange(formData.copy(tax = it)) }
            )

            Button(
                onClick = onAddProductClick,
                enabled = !isSubmitting && isFormValid(formData),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Product", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun isFormValid(formData: ProductFormData): Boolean {
    return formData.productName.isNotBlank() &&
            formData.productType.isNotBlank() &&
            formData.price.isNotBlank() &&
            formData.tax.isNotBlank()
}

@Composable
private fun ImagePicker(imageUri: Uri?, onImageUriChange: (Uri?) -> Unit) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> onImageUriChange(uri) }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { imagePickerLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select an image", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailsForm(
    productName: String,
    productType: String,
    price: String,
    tax: String,
    onProductNameChange: (String) -> Unit,
    onProductTypeChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onTaxChange: (String) -> Unit
) {
    val productTypes = listOf("Product", "Service")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = productName,
            onValueChange = onProductNameChange,
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = productName.isEmpty()
        )

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = productType,
                onValueChange = {},
                label = { Text("Product Type") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                isError = productType.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
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

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = price,
                onValueChange = { newValue ->
                    // Only allow numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onPriceChange(newValue)
                    }
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = price.isEmpty()
            )
            OutlinedTextField(
                value = tax,
                onValueChange = { newValue ->
                    // Only allow numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onTaxChange(newValue)
                    }
                },
                label = { Text("Tax (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = tax.isEmpty()
            )
        }
    }
}