package com.si.swipe_test.di

import androidx.room.Room
import androidx.work.WorkManager
import com.si.swipe_test.data.ApiService
import com.si.swipe_test.data.AppDatabase
import com.si.swipe_test.repo.ProductRepository
import com.si.swipe_test.data.SyncWorker
import com.si.swipe_test.viewmodel.ProductViewModel
import com.si.swipe_test.utils.ConnectivityManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    viewModel { ProductViewModel(get(), get(), get()) }
}

val dataModule = module {
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().productDao() }

    single { ProductRepository(get(), get(), get(), androidContext()) }

    single { ConnectivityManager(androidContext()) }
}

val workManagerModule = module {
    single { WorkManager.getInstance(androidContext()) }
    worker { SyncWorker(androidContext(), get(), get(), get(), get()) }
}