package com.example.trackerextension

import android.content.Context
import android.util.Log
import net.kollnig.missioncontrol.analysis.TrackerLibraryAnalyser

class TrackerControl(private val context: Context) : TrackersAccessPoint {
    private var running = false
    private var cancelled = false
    private var analyser = TrackerLibraryAnalyser(context)
    private var analyseResult: List<String>? = null

    override suspend fun analyse(appIds: List<String>, listener: TrackerListener) {
        Log.d("TrackerControl", "Starting analyse, $this, ${appIds.size}")

        var progress: Double
        if (!cancelled) {
            running = true
            run loop@{
                if (appIds.isNotEmpty()) {
                    appIds.forEachIndexed { index, packageId ->
                        Log.d("TrackerControl", "Analysing: $index, $packageId, $this")
                        if (cancelled) {
                            running = false
                            cancelled = false
                            progress = 0.0
                            return@loop
                        }

                        analyseResult = try {
                            analyser.analyse(packageId).split("\n")
                        } catch (e: Throwable) {
                            Log.e("TrackerControl", "analyse: failed, reason - ", e)
                            null
                        }

                        progress = ((index + 1) / appIds.size.toDouble()) * 100.0

                        if (!analyseResult.isNullOrEmpty()) {
                            val trackers = parseAnalyseResult(analyseResult)

                            if (trackers != null) {
                                val update = TrackerScanUpdate(
                                    TrackersAnalyseResult(
                                        packageId,
                                        trackers,
                                        String.format(
                                            context.getString(R.string.up_av_scanned_s),
                                            packageId
                                        )
                                    ), progress = progress
                                )

                                Log.d("TrackerControl", "onTrackerFound - ${update.upTracker}")

                                listener.onTrackerFound(update)
                                analyseResult = null
                            }
                        }

                        listener.trackerOnProgress(progress)

                        if (index == (appIds.size - 1)) {
                            running = false
                            listener.trackerOnFinish()
                        }
                    }
                } else {
                    listener.trackerOnProgress(100.0)
                    running = false
                    cancelled = false
                    listener.trackerOnFinish()
                }
            }
        } else {
            running = false
            cancelled = false
            progress = 0.0
        }
    }

    private fun parseAnalyseResult(analyseResult: List<String>?): List<Tracker>? {
        val trackers = mutableListOf<Tracker>()
        analyseResult?.forEach {
            if (it == "None") {
                return null
            }

            if (it.startsWith("•")) {
                val trackerName = it.replace("•", "").trim()
                trackers.add(Tracker(trackerName))
            }
        }
        return trackers
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun setRunning(running: Boolean) {
        this.running = running
    }

    override fun cancel() {
        cancelled = true
        running = false
    }
}

data class Tracker(val name: String)

data class TrackersAnalyseResult(
    val appId: String,
    val trackers: List<Tracker>,
    val message: String
)

