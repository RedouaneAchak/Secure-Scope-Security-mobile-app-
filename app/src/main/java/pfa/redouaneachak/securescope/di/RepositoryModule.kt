package pfa.redouaneachak.securescope.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pfa.redouaneachak.securescope.data.repository.ActiveAppsRepository
import pfa.redouaneachak.securescope.data.repository.ActiveAppsRepositoryImpl
import pfa.redouaneachak.securescope.data.repository.AppRepository
import pfa.redouaneachak.securescope.data.repository.AppRepositoryImpl
import pfa.redouaneachak.securescope.data.repository.HardwareMonitorRepository
import pfa.redouaneachak.securescope.data.repository.HardwareMonitorRepositoryImpl
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepositoryImpl
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindHardwareMonitorRepository(impl: HardwareMonitorRepositoryImpl): HardwareMonitorRepository

    @Binds
    @Singleton
    abstract fun bindSecurityScanRepository(impl: SecurityScanRepositoryImpl): SecurityScanRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitorRepository(impl: NetworkMonitorRepositoryImpl): NetworkMonitorRepository

    @Binds
    @Singleton
    abstract fun bindActiveAppsRepository(impl: ActiveAppsRepositoryImpl): ActiveAppsRepository
}