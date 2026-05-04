package com.bonial.feature.detail

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation key that carries the character ID into the detail screen.
 *
 * Owned by :feature:detail because it is the AssistedInject parameter for
 * [CharacterDetailViewModel]. :app's navigation graph imports this type when
 * wiring the back-stack push from the characters list.
 */
@Serializable
data class CharacterDetailKey(
    val id: Int,
) : NavKey
