package com.devtalk.messenger.di

import android.content.Context
import com.devtalk.messenger.data.repository.FirebaseRepository
import com.devtalk.messenger.data.repository.UserPreferences
import com.devtalk.messenger.webrtc.WebRtcManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseRepository(): FirebaseRepository {
        return FirebaseRepository()
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideWebRtcManager(
        @ApplicationContext context: Context,
        firebaseRepository: FirebaseRepository
    ): WebRtcManager {
        return WebRtcManager(context, firebaseRepository)
    }
}
