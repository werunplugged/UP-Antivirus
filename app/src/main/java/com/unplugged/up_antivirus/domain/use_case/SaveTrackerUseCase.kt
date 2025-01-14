package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.tracker.TrackerRepository
import com.example.trackerextension.TrackerModel
import javax.inject.Inject

class SaveTrackerUseCase @Inject constructor(private val trackerRepository: TrackerRepository) {
    suspend operator fun invoke(tracker: TrackerModel) {
        trackerRepository.insert(tracker)
    }

    suspend fun deleteAllForScan(scanId: Int) {
        trackerRepository.deleteAllForScan(scanId)
    }
}