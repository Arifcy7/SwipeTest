package com.si.swipe_test.ui.product

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.si.swipe_test.R
import com.si.swipe_test.data.Product
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel = koinViewModel()) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Add Product State
    val formData by viewModel.formData.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val addSuccess by viewModel.addSuccess.collectAsState()
    val addError by viewModel.addError.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Products", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add Product", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Search Products") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                shape = RoundedCornerShape(16.dp)
            )
            when {
                isLoading && products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                }

                products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No products yet. Add your first product!", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = products,
                            key = { product -> product.localId }
                        ) { product ->
                            ProductListItem(product = product)
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddProductSheet(
            formData = formData,
            isSubmitting = isSubmitting,
            onFormChange = viewModel::onFormChange,
            onAddProductClick = { viewModel.addProduct() },
            onDismiss = {
                showBottomSheet = false
                viewModel.resetAddState()
            }
        )
    }

    if (addSuccess) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetAddState()
                showBottomSheet = false
            },
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
fun ProductListItem(product: Product) {
    val context = LocalContext.current

    // Determine which image to show: server image, local URI, or placeholder
    val imageData = when {
        !product.image.isNullOrBlank() -> product.image
        !product.imageUri.isNullOrBlank() -> Uri.parse(product.imageUri)
        else -> R.drawable.ic_placeholder
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = product.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 400f
                        )
                    )
            )

            if (!product.isSynced) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sync),
                    contentDescription = "Not Synced",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(5.dp)
                        .size(18.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = product.productName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.productType,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = String.format("$%.2f", product.price),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}