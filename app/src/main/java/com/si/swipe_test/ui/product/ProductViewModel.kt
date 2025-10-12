package com.si.swipe_test.ui.product

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.si.swipe_test.data.Product
import com.si.swipe_test.data.ProductRepository
import com.si.swipe_test.data.SyncWorker
import com.si.swipe_test.model.ProductFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel constructor(
    private val repository: ProductRepository,
    private val application: Application,
    private val workManager: WorkManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val products: StateFlow<List<Product>> = searchQuery.combine(repository.getProducts()) { query, products ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.productName.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Other state flows remain the same
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Add Product Form State
    private val _formData = MutableStateFlow(ProductFormData())
    val formData = _formData.asStateFlow()

    // Add Product Action State
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess = _addSuccess.asStateFlow()

    private val _addError = MutableStateFlow<String?>(null)
    val addError = _addError.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (isNetworkAvailable()) {
                    repository.refreshProducts()
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh products."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFormChange(newFormData: ProductFormData) {
        _formData.value = newFormData
    }

    fun addProduct() {
        val currentFormData = _formData.value
        if (currentFormData.productName.isBlank() || currentFormData.price.isBlank() || currentFormData.tax.isBlank()) {
            _addError.value = "Product name, price, and tax cannot be empty."
            return
        }

        val newProduct = Product(
            productName = currentFormData.productName,
            productType = currentFormData.productType.ifBlank { "Product" },
            price = currentFormData.price.toDouble(),
            tax = currentFormData.tax.toDouble(),
            image = currentFormData.imageUri?.toString()
        )

        viewModelScope.launch {
            repository.saveProductLocally(newProduct)
            scheduleSync()
            _addSuccess.value = true
        }
    }

    fun resetAddState() {
        _addSuccess.value = false
        _addError.value = null
        _formData.value = ProductFormData()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncWorkRequest)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
