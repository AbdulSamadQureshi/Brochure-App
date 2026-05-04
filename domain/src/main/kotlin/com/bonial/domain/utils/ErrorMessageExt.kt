package com.bonial.domain.utils

import com.bonial.domain.model.network.response.ApiError

/**
 * Converts a nullable [ApiError] to a user-facing error message string.
 *
 * Lives in :domain so both :feature:characters and :feature:detail can import it
 * without coupling to each other or to :app.
 */
fun ApiError?.toErrorMessage(fallback: String = "An unknown error occurred"): String = this?.message ?: fallback
