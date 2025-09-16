package com.maintainer.app.di

import com.maintainer.app.data.remote.NhtsaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        // Only add logging interceptor in debug builds
        // Note: BuildConfig not available during compilation, using hardcoded debug logging for now
        builder.addInterceptor(
            HttpLoggingInterceptor().apply {
                // Note: Logging level set to HEADERS for security in production
                level = HttpLoggingInterceptor.Level.HEADERS // Reduced from BODY for security
            }
        )

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://vpic.nhtsa.dot.gov/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNhtsaApiService(retrofit: Retrofit): NhtsaApiService {
        return retrofit.create(NhtsaApiService::class.java)
    }
}