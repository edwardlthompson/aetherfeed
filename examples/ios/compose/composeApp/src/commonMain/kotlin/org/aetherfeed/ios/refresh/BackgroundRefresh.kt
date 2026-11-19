package org.aetherfeed.ios.refresh

/**
 * News refresh scheduling (DESIGN_BRIEF §8, decision 18, TECH_DECISION §5).
 *
 * - **Foreground:** always refresh when app becomes active (no remote push wake; decision 19).
 * - **Background:** `BGAppRefreshTask` best-effort only — iOS may defer or skip.
 * - **No APNs** until a paid Apple Developer account exists (`[HUMAN]`).
 */
data class BackgroundRefreshPolicy(
    val foregroundRefreshEnabled: Boolean = true,
    val bgAppRefreshRequested: Boolean = true,
)

fun shouldRefreshOnForeground(policy: BackgroundRefreshPolicy): Boolean =
    policy.foregroundRefreshEnabled

/** Gate duplicate refresh if a foreground pass completed within this window (ms). */
const val FOREGROUND_REFRESH_DEBOUNCE_MS = 30_000L