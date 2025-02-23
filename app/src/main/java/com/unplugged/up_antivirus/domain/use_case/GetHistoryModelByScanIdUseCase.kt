package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.history.model.HistoryModel
import javax.inject.Inject

class GetHistoryModelByScanIdUseCase @Inject constructor(private val historyUseCase: GetHistoryUseCase) {
    suspend operator fun invoke(scanId: Int): HistoryModel? {
        return historyUseCase.getHistoryById(scanId)
    }
}