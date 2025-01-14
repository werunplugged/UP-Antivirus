package com.unplugged.up_antivirus.domain.use_case

import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.tracker.TrackerRepository
import com.example.trackerextension.TrackerModel
import javax.inject.Inject

class GetTrackersByScanIdUseCase @Inject constructor(private val trackerRepository: TrackerRepository) {

    operator fun invoke(scanId: Int): Flow<List<TrackerModel>> {
        return trackerRepository.getByScanId(scanId)
    }
}