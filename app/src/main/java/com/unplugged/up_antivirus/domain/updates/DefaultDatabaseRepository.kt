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
) : DatabaseRepository {

    override suspend fun getDatabaseVersion(module: ScannerType): Int {
        return remoteDataStore.getDatabaseVersion(module).version
    }

    override fun errorNotification(title: String, subtitle: String) {
        notificationManager.errorNotification(title, subtitle)
    }

    override suspend fun isDatabaseLoaded(): Boolean {
        return hypatia.isDatabaseLoaded()
    }

    override suspend fun updateDatabase(attToken: String, userToken: String): UpdateResult {
        return withContext(Dispatchers.IO) {
            var isResumed = false
            suspendCoroutine { continuation ->
                hypatia.updateDatabase(attToken, userToken, object : UpdateListener {
                    override fun onSuccess() {
                        synchronized(this) {
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(UpdateResult.SUCCESS)
                            }
                        }
                    }

                    override fun onUnauthorized() {
                        synchronized(this) {
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(UpdateResult.UNAUTHORIZED)
                            }
                        }
                    }

                    override fun onFailure() {
                        synchronized(this) {
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(UpdateResult.FAILURE)
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
}
