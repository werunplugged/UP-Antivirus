package com.unplugged.up_antivirus.domain.updates

import com.unplugged.hypatia_extensions.HypatiaAccessPoint
import com.unplugged.up_antivirus.common.notifications.NotificationManager
import com.unplugged.upantiviruscommon.datastore.RemoteDataStore
import com.unplugged.upantiviruscommon.model.ScannerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import us.spotco.malwarescanner.Database.UpdateListener
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DefaultDatabaseRepository @Inject constructor(
    private val remoteDataStore: RemoteDataStore,
    private val hypatia: HypatiaAccessPoint,
    private val notificationManager: NotificationManager
//  private val blacklistLocalDataStore: BlacklistLocalDataStore
) : DatabaseRepository {

    //    override suspend fun updateBlacklistDatabase(currentVersion: Int): Int {
//        //  val dataBaseInfo = remoteDataStore.fetchBlackListData(currentVersion)
//        val blacklistVersion = remoteDataStore.getBlacklistVersion()
//        if (blacklistVersion > currentVersion) {
//            val dataBaseInfo = remoteDataStore.fetchBlacklistData(currentVersion)
//            try {
//                if (dataBaseInfo.toAdd.isNotEmpty()) {
//                    blacklistLocalDataStore.insertToDatabase(dataBaseInfo.toAdd)
//                }
//                if (dataBaseInfo.toRemove.isNotEmpty()) {
//                    blacklistLocalDataStore.removeFromDatabase(dataBaseInfo.toRemove)
//                }
//                return dataBaseInfo.version
//            } catch (e: Exception) {
//                return currentVersion
//            }
//        }
//        return currentVersion
//    }

    override suspend fun getDatabaseVersion(module: ScannerType): Int {
        return remoteDataStore.getDatabaseVersion(module).version
    }

    override fun errorNotification(title: String, subtitle: String) {
        notificationManager.errorNotification(title, subtitle)
    }

    override suspend fun isDatabaseLoaded(): Boolean {
        return hypatia.isDatabaseLoaded()
    }

    override suspend fun updateDatabase(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            var isResumed = false
            suspendCoroutine { continuation ->
                hypatia.updateDatabase(token, object : UpdateListener {
                    override fun onSuccess() {
                        synchronized(this) {
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(true)
                            }
                        }
                    }

                    override fun onFailure() {
                        synchronized(this) {
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(false)
                            }
                        }
                    }
                })
            }
        }
    }

    override suspend fun loadDatabase() {
        hypatia.loadDatabase()
    }

    override suspend fun isDatabaseAvailable(): Boolean {
        return hypatia.isDatabaseAvailable()
    }

    override suspend fun downloadHypatiaFiles() {
        //  remoteDataStore.downloadFiles()
    }
}
