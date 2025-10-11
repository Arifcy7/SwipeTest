package com.si.swipe_test.ui.product

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.swipe_test.data.Product
import com.si.swipe_test.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val application: Application
) : ViewModel() {

    // Product List State
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Add Product Form State
    private val _productName = MutableStateFlow("")
    val productName = _productName.asStateFlow()

    private val _productType = MutableStateFlow("Product")
    val productType = _productType.asStateFlow()

    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    private val _tax = MutableStateFlow("")
    val tax = _tax.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    // Add Product Action State
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess = _addSuccess.asStateFlow()

    private val _addError = MutableStateFlow<String?>(null)
    val addError = _addError.asStateFlow()

    init {
        getProducts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onProductNameChange(name: String) {
        _productName.value = name
    }

    fun onProductTypeChange(type: String) {
        _productType.value = type
    }

    fun onPriceChange(price: String) {
        _price.value = price
    }

    fun onTaxChange(tax: String) {
        _tax.value = tax
    }

    fun onImageUriChange(uri: Uri?) {
        _imageUri.value = uri
    }

    fun getProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _products.value = repository.getProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching products", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProduct() {
        if (_productName.value.isBlank() || _price.value.isBlank() || _tax.value.isBlank()) {
            _addError.value = "Product name, price, and tax cannot be empty."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _addError.value = null
            _addSuccess.value = false
            try {
                val nameBody = _productName.value.toRequestBody("text/plain".toMediaTypeOrNull())
                val typeBody = _productType.value.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = _price.value.toRequestBody("text/plain".toMediaTypeOrNull())
                val taxBody = _tax.value.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = _imageUri.value?.let { uri ->
                    application.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val imageBytes = inputStream.readBytes()
                        val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("files[]", "image.jpg", requestBody)
                    }
                }

                val response = repository.addProduct(nameBody, typeBody, priceBody, taxBody, imagePart)
                if (response.success) {
                    _addSuccess.value = true
                    getProducts() // Refresh the product list
                } else {
                    _addError.value = response.message
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error adding product", e)
                _addError.value = e.message
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun resetAddState() {
        _addSuccess.value = false
        _addError.value = null
        _productName.value = ""
        _productType.value = "Product"
        _price.value = ""
        _tax.value = ""
        _imageUri.value = null
    }
}
