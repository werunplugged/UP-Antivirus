package com.unplugged.up_antivirus.ui.settings.scheduler

enum class ScheduleChoice(val choice: Int) {
    Daily(0),
    ThreeTimesAWeek(1),
    Weekly(2),
    Disabled(3);

    companion object {
        fun fromValue(value: Int): ScheduleChoice {
            return entries.find { it.choice == value } ?: Disabled
        }
    }
}