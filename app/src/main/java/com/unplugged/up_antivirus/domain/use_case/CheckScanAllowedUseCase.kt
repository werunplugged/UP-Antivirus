package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.account.UpAccount
import com.unplugged.upantiviruscommon.utils.DateTimeUtils
import com.unplugged.upantiviruscommon.model.Resource
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class CheckScanAllowedUseCase @Inject constructor(
    private val getLatestScanUseCase: GetLatestScanUseCase) {

    suspend operator fun invoke(): Resource<Boolean> {

        return Resource.success(true)

        val subscription = UpAccount.getAccountSubscription()
        return when {
            subscription == null -> {
                Resource.error(IllegalStateException("NO_SUBSCRIPTION"))
            }

            subscription.id == "BASIC" -> {
                val lastScan = getLatestScanUseCase()
                if (lastScan != null) {
                    val lastScanDate = DateTimeUtils.stringToDate(lastScan.date)

                    //If failed to parse date, let the user scan?
                    if (lastScanDate == null) {
                        Resource.success(true)
                    } else {
                        val lastScanCal = Calendar.getInstance().also { it.time = lastScanDate }
                        val nowCal = Calendar.getInstance().also { it.time = Date() }

                        if (lastScanCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)) {
                            //The last scan MONTH is same as current MONTH, user can't scan
                            //Resource.success(false)
                            Resource.error(IllegalStateException("SCAN_LIMIT_REACHED"))
                        } else {
                            //The last scan MONTH is not same as current, user can scan.
                            Resource.success(true)
                        }
                    }
                } else {
                    //No scans yet performed, user can scan.
                    Resource.success(true)
                }

                //TODO later when server side ready, the check should be against the server not only local data.
                /*val remainingScans = accountRepository.getRemainingScans()
                if (remainingScans > 0) {
                    Resource.success(true)
                } else {
                    Resource.error(IllegalStateException("SCAN_LIMIT_REACHED"))
                }*/
            }

            else -> Resource.success(true)
        }
    }
}