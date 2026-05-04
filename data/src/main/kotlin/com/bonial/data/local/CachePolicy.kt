package com.bonial.data.local

import java.util.concurrent.TimeUnit

/**
 * Shared TTL constants for the local Room cache.
 *
 * Any repository that caches network responses should use these values so the
 * app has a single, auditable place to adjust cache lifetimes.
 */
object CachePolicy {
    /** Characters list page is considered fresh for 30 minutes after it was cached. */
    val CHARACTER_TTL_MS: Long = TimeUnit.MINUTES.toMillis(30)

    /** Returns true when [cachedAt] is still within [ttlMs] of [now]. */
    fun isFresh(
        cachedAt: Long,
        ttlMs: Long = CHARACTER_TTL_MS,
        now: Long = System.currentTimeMillis(),
    ): Boolean = (now - cachedAt) < ttlMs
}
