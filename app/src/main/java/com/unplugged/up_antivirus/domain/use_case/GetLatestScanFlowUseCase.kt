package com.unplugged.up_antivirus.domain.use_case

import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.history.HistoryRepository
import javax.inject.Inject

class GetLatestScanFlowUseCase @Inject constructor(private val historyRepository: HistoryRepository) {
    operator fun invoke(): Flow<HistoryModel?> {
        return historyRepository.getLatestAsFlow()
    }
}