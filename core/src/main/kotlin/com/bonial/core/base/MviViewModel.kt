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
 * [State] represents the UI State.
 * [Intent] represents the User Intent.
 * [Effect] represents the Side Effect (one-time events).
 */
abstract class MviViewModel<State, Intent, Effect> : ViewModel() {
    private val initialState: State by lazy { createInitialState() }

    abstract fun createInitialState(): State

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _intentFlow = Channel<Intent>(Channel.BUFFERED)
    val intentFlow = _intentFlow.receiveAsFlow()

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    init {
        subscribeIntents()
    }

    private fun subscribeIntents() {
        viewModelScope.launch {
            intentFlow.collect {
                handleIntent(it)
            }
        }
    }

    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            _intentFlow.send(intent)
        }
    }

    protected fun setState(reduce: State.() -> State) {
        _uiState.update { it.reduce() }
    }

    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch {
            _effect.send(effectValue)
        }
    }

    abstract fun handleIntent(intent: Intent)
}
