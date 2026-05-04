package com.bonial.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * All status-related colours in one place.
 *
 * Shared by :feature:characters (list badge) and :feature:detail (detail chip)
 * so status colour definitions never drift apart between screens.
 */
data class StatusColorSet(
    /** Dot / solid indicator colour. */
    val dot: Color,
    /** Chip background (soft tint). Used on the detail screen. */
    val background: Color,
    /** Readable label colour on top of [background]. Used on the detail screen. */
    val label: Color,
)

/**
 * Maps a raw status string (case-insensitive) to its [StatusColorSet].
 * Unknown or null values fall back to the neutral grey palette.
 */
fun String?.toStatusColorSet(): StatusColorSet =
    when (this?.lowercase()) {
        "alive" ->
            StatusColorSet(
                dot = StatusAlive,
                background = StatusAliveBg,
                label = StatusAliveText,
            )
        "dead" ->
            StatusColorSet(
                dot = StatusDead,
                background = StatusDeadBg,
                label = StatusDeadText,
            )
        else ->
            StatusColorSet(
                dot = StatusUnknown,
                background = StatusUnknownBg,
                label = StatusUnknownText,
            )
    }
