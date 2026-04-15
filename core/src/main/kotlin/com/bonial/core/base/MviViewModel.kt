package com.bonial.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI architecture.
 * [S] represents the UI State.
 * [I] represents the User Intent.
 * [E] represents the Side Effect (one-time events).
 */
abstract class MviViewModel<S, I, E> : ViewModel() {

    private val initialState: S by lazy { createInitialState() }
    abstract fun createInitialState(): S

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _intent = Channel<I>(Channel.BUFFERED)
    private val intent = _intent.receiveAsFlow()

    private val _effect = Channel<E>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    init {
        subscribeIntents()
    }

    private fun subscribeIntents() {
        viewModelScope.launch {
            intent.collect {
                handleIntent(it)
            }
        }
    }

    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.send(intent)
        }
    }

    protected fun setState(reduce: S.() -> S) {
        _uiState.update { it.reduce() }
    }

    protected fun setEffect(builder: () -> E) {
        val effectValue = builder()
        viewModelScope.launch {
            _effect.send(effectValue)
        }
    }

    abstract fun handleIntent(intent: I)
}
