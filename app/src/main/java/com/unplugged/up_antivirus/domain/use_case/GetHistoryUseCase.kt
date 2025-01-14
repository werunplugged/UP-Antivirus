package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.history.HistoryRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) {

    suspend operator fun invoke(): List<HistoryModel> {
        return historyRepository.getAll()
    }
}