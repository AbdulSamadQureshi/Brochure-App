package com.bonial.domain.utils

import com.bonial.domain.model.network.response.Request

/**
 * Transforms the [Request.Success] payload while preserving Loading and Error states unchanged.
 * Lives in domain so both domain use cases and data-layer repository implementations can use it
 * (data depends on domain, so the import is always available).
 */
inline fun <T, R> Request<T>.mapSuccess(transform: (T) -> R): Request<R> = when (this) {
    is Request.Loading -> Request.Loading
    is Request.Error -> Request.Error(apiError)
    is Request.Success -> Request.Success(transform(data))
}
