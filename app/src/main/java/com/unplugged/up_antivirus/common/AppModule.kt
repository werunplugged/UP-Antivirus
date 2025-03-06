package com.unplugged.up_antivirus.common

import android.content.Context
import androidx.work.WorkManager
import com.example.trackerextension.TrackerControl
import com.example.trackerextension.TrackerModel
import com.example.trackerextension.TrackersAccessPoint
import com.unplugged.hypatia_extensions.Hypatia
import com.unplugged.hypatia_extensions.HypatiaAccessPoint
import com.unplugged.signature_scanner.SignatureScannerAccessPoint
import com.unplugged.signature_scanner.appscanner.PackageScanner
import com.unplugged.signature_scanner.appscanner.ScanPackageTask
import com.unplugged.signature_scanner.database.BlacklistLocalSource
import com.unplugged.signature_scanner.database.BlacklistPackagesRoomSource
import com.unplugged.signature_scanner.database.BlacklistedPackageDao
import com.unplugged.signature_scanner.database.BlacklistLocalDataStore
import com.unplugged.upantiviruscommon.datastore.RemoteDataStore
import com.unplugged.signature_scanner.model.BlacklistMapper
import com.unplugged.signature_scanner.model.DefaultBlacklistPackageMapper
import com.unplugged.signature_scanner.repository.AppRepository
import com.unplugged.signature_scanner.repository.AppRepositoryImpl
import com.unplugged.signature_scanner.repository.BlacklistPackageRepository
import com.unplugged.signature_scanner.repository.DefaultBlacklistPackagesRepository
import com.unplugged.up_antivirus.common.notifications.NotificationManager
import com.unplugged.up_antivirus.data.AntivirusRoomDatabase
import com.unplugged.up_antivirus.data.account.AccountRemoteSource
import com.unplugged.up_antivirus.data.account.DefaultAccountRepository
import com.unplugged.up_antivirus.data.account.RetrofitAccountApi
import com.unplugged.up_antivirus.data.history.DefaultHistoryRepository
import com.unplugged.up_antivirus.data.history.HistoryDao
import com.unplugged.up_antivirus.data.history.HistoryLocalSource
import com.unplugged.up_antivirus.data.history.HistoryRoomSource
import com.unplugged.up_antivirus.data.history.model.DefaultHistoryMapper
import com.unplugged.up_antivirus.data.history.model.HistoryMapper
import com.unplugged.up_antivirus.data.malware.DefaultMalwareRepository
import com.unplugged.up_antivirus.data.malware.MalwareDao
import com.unplugged.up_antivirus.data.malware.MalwareLocalSource
import com.unplugged.up_antivirus.data.malware.MalwareRepository
import com.unplugged.up_antivirus.data.malware.MalwareRoomSource
import com.unplugged.up_antivirus.data.malware.model.DefaultMalwareMapper
import com.unplugged.up_antivirus.data.malware.model.MalwareMapper
import com.unplugged.up_antivirus.data.preferences.DefaultPreferencesRepository
import com.unplugged.up_antivirus.data.preferences.SharedPreferencesSource
import com.unplugged.up_antivirus.data.tracker.DefaultTrackerRepository
import com.unplugged.up_antivirus.data.tracker.TrackerDao
import com.unplugged.up_antivirus.data.tracker.TrackerLocalSource
import com.unplugged.up_antivirus.data.tracker.TrackerRepository
import com.unplugged.up_antivirus.data.tracker.TrackerRoomSource
import com.unplugged.up_antivirus.data.tracker.model.DefaultTrackerMapper
import com.unplugged.up_antivirus.data.tracker.model.TrackerMapper
import com.unplugged.up_antivirus.domain.account.AccountRepository
import com.unplugged.up_antivirus.data.history.HistoryRepository
import com.unplugged.up_antivirus.data.tracker.model.DefaultTrackerDetailsRepository
import com.unplugged.up_antivirus.data.tracker.model.TrackerDetailsRepository
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.up_antivirus.domain.use_case.CreateScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetActiveScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationInfoUseCase
import com.unplugged.up_antivirus.domain.use_case.GetSessionUseCase
import com.unplugged.up_antivirus.domain.use_case.GetSubscriptionDataUseCase
import com.unplugged.up_antivirus.domain.use_case.GetUserAppListForTrackersUseCase
import com.unplugged.up_antivirus.domain.use_case.LogoutUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveHistoryUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveMalwareUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveTrackerUseCase
import com.unplugged.up_antivirus.domain.use_case.StopScanServiceUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateDatabaseUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateScanDoneUseCase
import com.unplugged.up_antivirus.scanner.repository.DefaultScannerRepository
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import com.unplugged.up_antivirus.ui.scan.TrackerAdapter
import com.unplugged.up_antivirus.domain.updates.DatabaseRepository
import com.unplugged.up_antivirus.domain.updates.DefaultDatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManagerInstance(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        blacklistPackageRepository: BlacklistPackageRepository,
    ): AppRepository {
        return AppRepositoryImpl(
            context,
            blacklistPackageRepository,
        )
    }

    @Provides
    @Singleton
    fun provideDatabaseRepository(
        remoteDataStore: RemoteDataStore,
        hypatia: HypatiaAccessPoint,
        notificationManager: NotificationManager
        //blacklistLocalDataStore: BlacklistLocalDataStore
    ): DatabaseRepository {
        return DefaultDatabaseRepository(
            remoteDataStore,
            hypatia,
            notificationManager
        )
    }

    @Provides
    @Singleton
    fun provideTrackerDetailsRepository(): TrackerDetailsRepository {
        return DefaultTrackerDetailsRepository()
    }

    @Provides
    @Singleton
    fun provideBlackListPackageRepository(
        localSource: BlacklistLocalSource,
        mapper: BlacklistMapper
    ): BlacklistPackageRepository {
        return DefaultBlacklistPackagesRepository(localSource, mapper)
    }

    @Provides
    @Singleton
    fun provideBlackListPackageRoomSource(blacklistedPackageDao: BlacklistedPackageDao): BlacklistLocalSource {
        return BlacklistPackagesRoomSource(blacklistedPackageDao)
    }

    @Provides
    @Singleton
    fun provideBlacklistedPackageDao(database: AntivirusRoomDatabase): BlacklistedPackageDao {
        return database.getBlacklistPackageDao()
    }

    @Provides
    @Singleton
    fun provideTrackerDao(database: AntivirusRoomDatabase): TrackerDao {
        return database.getTrackerDao()
    }

    @Provides
    fun provideBlacklistMapper(): BlacklistMapper {
        return DefaultBlacklistPackageMapper()
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(
        localSource: HistoryLocalSource,
        mapper: HistoryMapper
    ): HistoryRepository {
        return DefaultHistoryRepository(localSource, mapper)
    }

    @Provides
    @Singleton
    fun provideMalwareRepository(
        localSource: MalwareLocalSource,
        mapper: MalwareMapper
    ): MalwareRepository {
        return DefaultMalwareRepository(localSource, mapper)
    }

    @Provides
    @Singleton
    fun providTrackerRepository(
        localSource: TrackerLocalSource,
        mapper: TrackerMapper
    ): TrackerRepository {
        return DefaultTrackerRepository(localSource, mapper)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountRemoteSource: AccountRemoteSource,
        sharedPreferencesSource: SharedPreferencesSource
    ): AccountRepository {
        return DefaultAccountRepository(accountRemoteSource, sharedPreferencesSource)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        sharedPreferencesSource: SharedPreferencesSource
    ): PreferencesRepository {
        return DefaultPreferencesRepository(sharedPreferencesSource)
    }

    @Provides
    @Singleton
    fun provideHistoryMapper(): HistoryMapper {
        return DefaultHistoryMapper()
    }

    @Provides
    @Singleton
    fun provideMalwareMapper(): MalwareMapper {
        return DefaultMalwareMapper()
    }

    @Provides
    @Singleton
    fun provideTrackerMapper(): TrackerMapper {
        return DefaultTrackerMapper()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: AntivirusRoomDatabase): HistoryDao {
        return database.getHistoryDao()
    }

    @Provides
    @Singleton
    fun provideMalwareLocalSource(dao: MalwareDao): MalwareLocalSource {
        return MalwareRoomSource(dao)
    }

    @Provides
    @Singleton
    fun provideTrackerLocalSource(dao: TrackerDao): TrackerLocalSource {
        return TrackerRoomSource(dao)
    }

    @Provides
    @Singleton
    fun provideHistoryLocalSource(dao: HistoryDao): HistoryLocalSource {
        return HistoryRoomSource(dao)
    }

    @Provides
    @Singleton
    fun provideMalwareDao(database: AntivirusRoomDatabase): MalwareDao {
        return database.getMalwareDao()
    }

    @Provides
    fun provideAppScannerUseCase(
        @ApplicationContext context: Context,
        appRepository: AppRepository
    ): ScanPackageTask {
        return ScanPackageTask(context, appRepository)
    }

    @Provides
    fun provideLogoutUseCase(
        workManager: WorkManager,
        accountRepository: DefaultAccountRepository,
        sharedPreferencesSource: SharedPreferencesSource,
        historyRepository: HistoryRepository,
        malwareRepository: MalwareRepository,
        scannerRepository: ScannerRepository
    ): LogoutUseCase {
        return LogoutUseCase(
            workManager,
            accountRepository,
            sharedPreferencesSource,
            historyRepository,
            malwareRepository,
            scannerRepository
        )
    }

    @Provides
    @Singleton
    fun provideMalwareRoomSource(dao: MalwareDao): MalwareRoomSource {
        return MalwareRoomSource(dao)
    }

    @Provides
    @Singleton
    fun provideTrackerRoomSource(dao: TrackerDao): TrackerRoomSource {
        return TrackerRoomSource(dao)
    }

    @Provides
    @Singleton
    fun provideAccountRemoteSource(
        @Named("base_url") baseUrl: String,
    ): AccountRemoteSource {
        return RetrofitAccountApi(baseUrl)
    }

    @Provides
    fun provideRemoteDataStore(): RemoteDataStore {
        return RemoteDataStore()
    }

    @Provides
    fun provideLocalDataStore(blacklistPackagesRepository: BlacklistPackageRepository): BlacklistLocalDataStore {
        return BlacklistLocalDataStore(blacklistPackagesRepository)
    }

    @Provides
    @Named("base_url")
    fun provideBaseUrl(@ApplicationContext context: Context): String {
        return context.getString(com.unplugged.account.R.string.base_url)
    }

    @Provides
    fun provideHypatiaAccessPoint(@ApplicationContext context: Context): HypatiaAccessPoint {
        return Hypatia(context)
    }

    @Singleton
    @Provides
    fun provideScannerRepository(
        hypatia: HypatiaAccessPoint,
        trackers: TrackersAccessPoint,
        blacklist: SignatureScannerAccessPoint,
        stringProvider: StringProvider,
        saveHistoryUseCase: SaveHistoryUseCase,
        updateScanDoneUseCase: UpdateScanDoneUseCase,
        saveMalwareUseCase: SaveMalwareUseCase,
        saveTrackerUseCase: SaveTrackerUseCase,
        stopScanServiceUseCase: StopScanServiceUseCase,
        getApplicationInfoUseCase: GetApplicationInfoUseCase,
        getUserAppListForTrackersUseCase: GetUserAppListForTrackersUseCase,
        notificationManager: NotificationManager,
        appRepository: AppRepository
    ): ScannerRepository {
        return DefaultScannerRepository(
            hypatia,
            trackers,
            blacklist,
            stringProvider,
            saveHistoryUseCase,
            updateScanDoneUseCase,
            saveMalwareUseCase,
            saveTrackerUseCase,
            stopScanServiceUseCase,
            getApplicationInfoUseCase,
            getUserAppListForTrackersUseCase,
            notificationManager,
            appRepository
        )
    }

    @Provides
    fun provideTrackersAccessPoint(@ApplicationContext context: Context): TrackersAccessPoint {
        return TrackerControl(context)
    }

    @Provides
    fun provideSignatureScannerAccessPoint(
        @ApplicationContext context: Context,
        appRepository: AppRepository
    ): SignatureScannerAccessPoint {
        return PackageScanner(context, appRepository)
    }

    @Provides
    fun provideSaveTrackerUseCase(trackerRepository: TrackerRepository): SaveTrackerUseCase {
        return SaveTrackerUseCase(trackerRepository)
    }

    @Provides
    fun provideCreateScanIdUseCase(scannerRepository: ScannerRepository): CreateScanIdUseCase {
        return CreateScanIdUseCase(scannerRepository)
    }

    @Provides
    fun provideGetActiveScanIdUseCase(scannerRepository: ScannerRepository): GetActiveScanIdUseCase {
        return GetActiveScanIdUseCase(scannerRepository)
    }

    @Provides
    @Singleton
    fun provideGetApplicationInfoUseCase(@ApplicationContext context: Context): GetApplicationInfoUseCase {
        return GetApplicationInfoUseCase(context)
    }

    @Provides
    @Singleton
    fun provideGetUserAppListForTrackersUseCase(@ApplicationContext context: Context): GetUserAppListForTrackersUseCase {
        return GetUserAppListForTrackersUseCase(context)
    }

    @Provides
    fun provideTrackerAdapterFactory(
        getApplicationIconUseCase: GetApplicationIconUseCase
    ): TrackerAdapter.Factory {
        return object : TrackerAdapter.Factory {
            override fun create(
                clickListener: (tracker: TrackerModel) -> Unit,
                trackers: MutableList<TrackerModel>
            ): TrackerAdapter {
                return TrackerAdapter(
                    clickListener = clickListener,
                    trackers = trackers,
                    getApplicationIconUseCase = getApplicationIconUseCase
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideGetApplicationIconUseCase(@ApplicationContext context: Context): GetApplicationIconUseCase {
        return GetApplicationIconUseCase(context)
    }

    @Provides
    @Singleton
    fun provideStopScanServiceUseCase(@ApplicationContext context: Context): StopScanServiceUseCase {
        return StopScanServiceUseCase(context)
    }

    @Provides
    @Singleton
    fun provideUpdateDatabaseUseCase(
        @ApplicationContext context: Context,
        preferencesRepository: PreferencesRepository,
        databaseRepository: DatabaseRepository
    ): UpdateDatabaseUseCase {
        return UpdateDatabaseUseCase(
            context,
            preferencesRepository,
            databaseRepository
        )
    }

    @Provides
    @Singleton
    fun provideFetchAuthData(): GetSessionUseCase {
        return GetSessionUseCase()
    }

    @Provides
    @Singleton
    fun provideFetchSubscriptionUseCase(): GetSubscriptionDataUseCase {
        return GetSubscriptionDataUseCase()
    }
}