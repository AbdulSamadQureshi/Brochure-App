package com.bonial.brochure.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Root destination for the character list screen.
 *
 * [CharacterDetailKey] lives in :feature:detail because it is also the
 * AssistedInject parameter for [com.bonial.feature.detail.CharacterDetailViewModel].
 */
@Serializable
data object CharacterListKey : NavKey
