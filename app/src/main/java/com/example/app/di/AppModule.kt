package com.example.app.di


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.algolia.search.saas.Client
import com.algolia.search.saas.Index
import com.example.data.FirebaseService
import com.example.data.IDatabaseService
import com.example.data.api.Api
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Object that configure the Dependency Injection using Hilt
 **/

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabaseService(): IDatabaseService = FirebaseService()

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()


    @Singleton
    @Provides
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun provideSharedPrefManager(sharedPreferences: SharedPreferences): SharedPrefManager {
        return SharedPrefManager(sharedPreferences)
    }

}
/** Module that provides dependencies for the Api interactions */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maverkick-api-mkqizzjwda-uc.a.run.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)  // Use the custom OkHttp client
            .build()
    }

    @Provides
    @Singleton
    fun provideVideoProcessingApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }
}

/** Module for Algolia Search */
@Module
@InstallIn(SingletonComponent::class)
object AlgoliaModule {

    @Provides
    @Singleton
    fun provideAlgoliaClient(): Client {
        return Client("E4XSC4B7C4", "244e61482e1bd3f71ea6c54f6a4ad00a")
    }

    @Provides
    fun provideAlgoliaIndex(client: Client): Index {
        return client.getIndex("courses")
    }
}
