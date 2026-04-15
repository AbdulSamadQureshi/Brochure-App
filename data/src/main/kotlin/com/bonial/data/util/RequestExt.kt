package com.bonial.data.util

import com.bonial.domain.model.network.response.Request

/**
 * Transforms the [Request.Success] payload while passing [Request.Loading] and
 * [Request.Error] through unchanged.
 *
 * Lives in :data because it operates on the network-response type and every
 * repository that wraps an API call needs it. Centralising it here means new
 * repositories get it for free without duplicating the three-branch `when`.
 */
internal inline fun <T, R> Request<T>.mapSuccess(transform: (T) -> R): Request<R> = when (this) {
    is Request.Loading -> Request.Loading
    is Request.Error -> Request.Error(apiError)
    is Request.Success -> Request.Success(transform(data))
}
