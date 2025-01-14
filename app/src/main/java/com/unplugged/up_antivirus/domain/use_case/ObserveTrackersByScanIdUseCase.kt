package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.data.tracker.TrackerRepository
import javax.inject.Inject

class ObserveTrackersByScanIdUseCase @Inject constructor(private val trackerRepository: TrackerRepository) {
    operator fun invoke(scanId: Int) = trackerRepository.observeTrackersByScanId(scanId)
}