package com.bonial.core.ui

/**
 * A sealed class representing the different states of a UI component.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()

    data class Error(
        val message: String? = null,
    ) : UiState<Nothing>()

    data class Success<out T>(
        val data: T,
    ) : UiState<T>()

    object Idle : UiState<Nothing>()
}
