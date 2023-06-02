package com.example.app.di

/**
 * Object that configure the Dependency Injection using Hilt
 **/
import android.content.Context
import android.content.SharedPreferences
import com.example.data.FirebaseService
import com.example.data.IDatabaseService
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideSharedPrefManager(sharedPreferences: SharedPreferences): SharedPrefManager {
        return SharedPrefManager(sharedPreferences)
    }

}
