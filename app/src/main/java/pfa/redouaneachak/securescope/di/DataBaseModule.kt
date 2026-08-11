package pfa.redouaneachak.securescope.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pfa.redouaneachak.securescope.data.local.AppDatabase
import pfa.redouaneachak.securescope.data.local.dao.NetworkSessionDao
import pfa.redouaneachak.securescope.data.local.dao.ScanResultDao
import pfa.redouaneachak.securescope.data.local.dao.TrackerDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "secure_scope_database").build()
    }

    @Provides
    fun provideScanResultDao(database: AppDatabase): ScanResultDao = database.scanResultDao()

    @Provides
    fun provideTrackerDao(database: AppDatabase): TrackerDao = database.trackerDao()

    @Provides
    fun provideNetworkSessionDao(database: AppDatabase): NetworkSessionDao = database.networkSessionDao()
}