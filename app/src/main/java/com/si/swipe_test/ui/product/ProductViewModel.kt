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

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

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

    fun addProduct(productName: String, productType: String, price: String, tax: String, imageUri: Uri?) {
        if (productName.isBlank() || price.isBlank() || tax.isBlank()) {
            _addError.value = "All fields must be filled"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _addError.value = null
            _addSuccess.value = false
            try {
                val nameBody = productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val typeBody = productType.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val taxBody = tax.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageUri?.let { uri ->
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
    }
}
