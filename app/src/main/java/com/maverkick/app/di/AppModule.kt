package com.maverkick.app.di


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.algolia.search.client.ClientSearch
import com.algolia.search.client.Index
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.maverkick.data.FirebaseService
import com.maverkick.data.IDatabaseService
import com.maverkick.data.api.ChatApi
import com.maverkick.data.api.CourseCreationApi
import com.maverkick.data.api.LessonApi
import com.maverkick.data.auth.AuthenticationService
import com.maverkick.data.auth.FirebaseAuthenticationService
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.crypto.AEADBadTagException
import javax.inject.Named
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
    fun provideAuthenticationService(firebaseAuth: FirebaseAuth): AuthenticationService {
        return FirebaseAuthenticationService(firebaseAuth)
    }

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
        // Try to create EncryptedSharedPreferences and catch any cryptographic exceptions.
        return try {
            EncryptedSharedPreferences.create(
                context,
                "encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: AEADBadTagException) {
            // Clear the default SharedPreferences or handle according to your needs.
            context.getSharedPreferences("default_prefs", Context.MODE_PRIVATE).edit().clear().apply()

            // Optionally, re-throw the exception or create a new instance of SharedPreferences
            // that does not use encryption, depending on how critical the encrypted preferences are to your app.
            context.getSharedPreferences("default_prefs", Context.MODE_PRIVATE)
        }
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
            .readTimeout(20, TimeUnit.MINUTES)
            .writeTimeout(20, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    @Named("LessonApiRetrofit")
    fun provideRetrofitForLessonApi(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lesson-api-mkqizzjwda-uc.a.run.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("ChatApiRetrofit")
    fun provideRetrofitForChatApi(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://chat-api-mkqizzjwda-uc.a.run.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("CourseCreationApiRetrofit")
    fun provideRetrofitForCourseCreationApi(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://course-generation-api-mkqizzjwda-uc.a.run.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }


    @Provides
    @Singleton
    fun provideLessonApi(@Named("LessonApiRetrofit") retrofitForLessonApi: Retrofit): LessonApi {
        return retrofitForLessonApi.create(LessonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApi(@Named("ChatApiRetrofit") retrofitForChatApi: Retrofit): ChatApi {
        return retrofitForChatApi.create(ChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseCreationApi(@Named("CourseCreationApiRetrofit") retrofitForCourseCreationApi: Retrofit): CourseCreationApi {
        return retrofitForCourseCreationApi.create(CourseCreationApi::class.java)
    }

}

/** Module for Algolia Search */
@Module
@InstallIn(SingletonComponent::class)
object AlgoliaModule {

    @Provides
    @Singleton
    fun provideAlgoliaClient(): ClientSearch {
        return ClientSearch(
            applicationID = ApplicationID("E4XSC4B7C4"),
            apiKey = APIKey("244e61482e1bd3f71ea6c54f6a4ad00a")
        )
    }

    @Provides
    fun provideAlgoliaIndex(client: ClientSearch): Index {
        return client.initIndex(indexName= IndexName("courses"))
    }
}
