package com.driftly.sleepsounds.di

import android.content.Context
import androidx.room.Room
import com.driftly.sleepsounds.data.database.DriftlyDatabase
import com.driftly.sleepsounds.data.database.MixDao
import com.driftly.sleepsounds.data.preferences.UserPreferences
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
    fun provideDatabase(@ApplicationContext context: Context): DriftlyDatabase {
        return Room.databaseBuilder(
            context,
            DriftlyDatabase::class.java,
            "driftly.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMixDao(database: DriftlyDatabase): MixDao {
        return database.mixDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}
