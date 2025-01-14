package com.unplugged.up_antivirus.domain.use_case

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.unplugged.account.UpAccount
import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.upantiviruscommon.model.Connectivity
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.up_antivirus.domain.updates.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateDatabaseUseCase @Inject constructor(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val databaseRepository: DatabaseRepository
) {

    private val BLACKLIST_KEY = "blackListKey"
    private val HYPATIA_KEY = "hypatiaKey"
    private val NEW_APP_DATA = "newAppDatabase"

    private var blackListVersion: Int? = 0
    private var localHypatiaVersion: Int = 0
    private var remoteHypatiaVersion: Int = 0

    suspend fun checkHypatiaDatabaseVersion(): Boolean {
        if (loadHypatiaDatabaseVersion() < 0) {
            saveHypatiaDatabaseVersion(localHypatiaVersion)
        }
        localHypatiaVersion = loadHypatiaDatabaseVersion()
        remoteHypatiaVersion = try {
            databaseRepository.getDatabaseVersion(ScannerType.HYPATIA)
        } catch (e: Exception) {
            Log.d("remoteHypatiaVersionError", "remoteHypatiaVersionError: $e")
            localHypatiaVersion
        }
        return remoteHypatiaVersion > localHypatiaVersion
    }

    suspend fun performUpdate(): Boolean {
        return withContext(Dispatchers.IO) {
            if (loadHypatiaDatabaseVersion() == -1) { //if we update from the old antivirus, the bin + sig files are not the same
                removeOldFiles()
            }
            try {
                val result = databaseRepository.updateDatabase(UpAccount.getSession()?.token.orEmpty())
                return@withContext if (result) {
                    saveHypatiaDatabaseVersion(remoteHypatiaVersion)
                    preferencesRepository.saveData(NEW_APP_DATA, true)
                    true
                } else {
                    Log.d("update error", "Update error")
                    localHypatiaVersion = loadHypatiaDatabaseVersion()
                    false
                }
            } catch (e: Exception) {
                Log.d("update error", "Update error: $e ")
                return@withContext false
            }
        }
    }

    private fun loadHypatiaDatabaseVersion(): Int {
        return preferencesRepository.loadDataVersion(HYPATIA_KEY)
    }

    private fun saveHypatiaDatabaseVersion(value: Int?) {
        if (value != null) {
            preferencesRepository.saveData(HYPATIA_KEY, value)
        }
    }

    fun internetConnectivity(): Connectivity {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return Connectivity.NONE
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return Connectivity.NONE

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> Connectivity.WIFI

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                    activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> Connectivity.CELLULAR

            else -> Connectivity.NONE
        }
    }

    private fun removeOldFiles() {
        val directory = File(context.filesDir, "signatures")
        val filesToDelete = directory.listFiles()
        filesToDelete?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    fun setAllowDownloadOverCellular(value: Boolean) {
        preferencesRepository.setAllowCellularDownload(value)
    }

    fun getAllowDownloadOverCellular(): Boolean {
        return preferencesRepository.getAllowCellularDownload()
    }

    fun isNewAppDatabase(): Boolean {
        return preferencesRepository.isNewAppDatabase(NEW_APP_DATA)
    }

    fun errorNotification(title: String, subtitle: String) {
        databaseRepository.errorNotification(title, subtitle)
    }

    suspend fun isDatabaseLoaded(): Boolean {
        return databaseRepository.isDatabaseLoaded()
    }

    suspend fun loadDatabase() {
        databaseRepository.loadDatabase()
    }
}
