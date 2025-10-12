package com.si.swipe_test.di

import androidx.room.Room
import androidx.work.WorkManager
import com.si.swipe_test.data.ApiService
import com.si.swipe_test.data.AppDatabase
import com.si.swipe_test.data.ProductRepository
import com.si.swipe_test.ui.product.ProductViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    viewModel { ProductViewModel(get(), get(), get()) }
}

val dataModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://app.getswipe.in/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "product-database"
        ).build()
    }
    single { get<AppDatabase>().productDao() }
    single { ProductRepository(get(), get()) }
}

val workManagerModule = module {
    single { WorkManager.getInstance(androidContext()) }
}
