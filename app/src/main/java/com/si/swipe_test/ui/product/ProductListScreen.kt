package com.si.swipe_test.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.si.swipe_test.R
import com.si.swipe_test.data.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val error by viewModel.error.collectAsState()

    // Add Product State
    val productName by viewModel.productName.collectAsState()
    val productType by viewModel.productType.collectAsState()
    val price by viewModel.price.collectAsState()
    val tax by viewModel.tax.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val addSuccess by viewModel.addSuccess.collectAsState()
    val addError by viewModel.addError.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add Product")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(query = searchQuery, onQueryChange = viewModel::onSearchQueryChange)
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: $error")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products) { product ->
                        ProductListItem(product = product)
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddProductSheet(
            productName = productName,
            productType = productType,
            price = price,
            tax = tax,
            imageUri = imageUri,
            isSubmitting = isSubmitting,
            onProductNameChange = viewModel::onProductNameChange,
            onProductTypeChange = viewModel::onProductTypeChange,
            onPriceChange = viewModel::onPriceChange,
            onTaxChange = viewModel::onTaxChange,
            onImageUriChange = viewModel::onImageUriChange,
            onAddProductClick = { viewModel.addProduct() },
            onDismiss = {
                showBottomSheet = false
                viewModel.resetAddState()
            }
        )
    }

    if (addSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetAddState() },
            title = { Text("Success") },
            text = { Text("Product added successfully!") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.resetAddState()
                    showBottomSheet = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    addError?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.resetAddState() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { viewModel.resetAddState() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search Products") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
fun ProductListItem(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.image)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = product.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = product.productName, style = MaterialTheme.typography.titleMedium)
                Text(text = "Price: ${product.price}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
