package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.history.HistoryRepository
import javax.inject.Inject

class GetLatestScanUseCase @Inject constructor(private val historyRepository: HistoryRepository) {

    suspend operator fun invoke(): HistoryModel? {
        return historyRepository.getLatest()
    }
}