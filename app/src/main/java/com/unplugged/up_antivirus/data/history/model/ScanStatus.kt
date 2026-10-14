package com.unplugged.up_antivirus.data.history.model

/**
 * Lifecycle of a scan-history row (UNP-8704).
 *
 * Stored as [code] rather than the enum ordinal so the values survive reordering. COMPLETED is 0
 * because that is the DEFAULT the v4 -> v5 migration backfills onto pre-existing rows, which were
 * written before this column existed and cannot be classified retroactively.
 */
enum class ScanStatus(val code: Int) {
    COMPLETED(0),

    /** Written when the scan starts; replaced when it finishes, is cancelled, or is swept. */
    RUNNING(1),

    /** The user cancelled the scan. */
    CANCELLED(2),

    /**
     * The process died mid-scan (e.g. the OOM in UNP-8704). Nothing can still be RUNNING in a
     * fresh process, so any row left RUNNING at startup is swept to this.
     */
    INTERRUPTED(3);

    companion object {
        fun fromCode(code: Int): ScanStatus = values().firstOrNull { it.code == code } ?: COMPLETED
    }
}
