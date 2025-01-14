package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.history.HistoryRepository
import javax.inject.Inject

class SaveHistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) {

    suspend operator fun invoke(historyModel: HistoryModel): Int {
        return historyRepository.insert(historyModel)
    }

    suspend fun update(historyModel: HistoryModel) {
        historyRepository.update(historyModel)
    }

    suspend fun delete(historyModel: HistoryModel) {
        historyRepository.delete(historyModel)
    }

    suspend fun getLastEntryId(): Int? {
        return historyRepository.getLastEntryId()
    }
}