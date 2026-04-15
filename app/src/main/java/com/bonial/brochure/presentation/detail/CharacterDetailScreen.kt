package com.bonial.brochure.presentation.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.bonial.brochure.R
import com.bonial.brochure.presentation.home.CharacterDetailIntent
import com.bonial.brochure.presentation.home.CharacterDetailViewModel
import com.bonial.brochure.presentation.home.ErrorMessage
import com.bonial.brochure.presentation.home.ImageErrorPlaceholder
import com.bonial.brochure.presentation.model.CharacterDetailUi
import com.bonial.brochure.presentation.theme.StatusAliveBg
import com.bonial.brochure.presentation.theme.StatusAliveText
import com.bonial.brochure.presentation.theme.StatusAlive
import com.bonial.brochure.presentation.theme.StatusDeadBg
import com.bonial.brochure.presentation.theme.StatusDeadText
import com.bonial.brochure.presentation.theme.StatusDead
import com.bonial.brochure.presentation.theme.StatusUnknownBg
import com.bonial.brochure.presentation.theme.StatusUnknownText
import com.bonial.brochure.presentation.theme.StatusUnknown
import com.bonial.core.ui.extensions.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.character?.name ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.label_back),
                        )
                    }
                },
                actions = {
                    state.character?.let { character ->
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append(character.name ?: "")
                                    character.species?.let { append(" · $it") }
                                    character.status?.let { append(" · $it") }
                                    character.imageUrl?.let { append("\n$it") }
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, null))
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.label_share),
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.sendIntent(CharacterDetailIntent.ToggleFavourite) },
                    ) {
                        Icon(
                            imageVector = if (state.isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(
                                if (state.isFavourite) R.string.content_desc_unfavourite else R.string.content_desc_favourite,
                            ),
                            tint = if (state.isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            val character = state.character
            when {
                state.isLoading -> CharacterDetailShimmer()
                state.error != null -> ErrorMessage(
                    message = state.error,
                    onRetry = { viewModel.sendIntent(CharacterDetailIntent.Retry) },
                )
                character != null -> CharacterDetailContent(character = character)
            }
        }
    }
}

/**
 * Skeleton loader that mirrors the real detail layout so there's no jarring
 * layout shift when content arrives.
 */
@Composable
private fun CharacterDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("detail_shimmer"),
    ) {
        // Hero image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .shimmerEffect(),
        )
        // Metadata rows placeholder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Name shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(24.dp)
                    .shimmerEffect(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Detail row shimmers
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .shimmerEffect(),
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CharacterDetailContent(character: CharacterDetailUi) {
    var imageLoaded by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        ) {
            // Shimmer shown while image is in-flight
            if (!imageLoaded && !imageError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect(),
                )
            }

            // Actual image — placeholder_image shown by Coil while network is loading;
            // we track Success/Error to hide the shimmer and show the error overlay.
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(character.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .crossfade(400)
                    .build(),
                contentDescription = character.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Success -> {
                            imageLoaded = true
                            imageError = false
                        }
                        is AsyncImagePainter.State.Error -> {
                            imageError = true
                            imageLoaded = false
                        }
                        else -> Unit
                    }
                },
            )

            // Error overlay — shown when Coil could not load the image
            if (imageError) {
                ImageErrorPlaceholder(modifier = Modifier.fillMaxSize())
            }
        }

        // Content card slides up after image
        AnimatedVisibility(
            visible = contentVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 400),
            ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                // Name + status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = character.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    character.status?.let {
                        StatusChip(status = it)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                character.species?.let { DetailRow(label = "Species", value = it) }
                character.gender?.let { DetailRow(label = "Gender", value = it) }
                character.origin?.let { DetailRow(label = "Origin", value = it) }
                character.location?.let { DetailRow(label = "Last known location", value = it) }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (bgColor, dotColor) = when (status.lowercase()) {
        "alive" -> StatusAliveBg to StatusAlive
        "dead" -> StatusDeadBg to StatusDead
        else -> StatusUnknownBg to StatusUnknown
    }
    val textColor = when (status.lowercase()) {
        "alive" -> StatusAliveText
        "dead" -> StatusDeadText
        else -> StatusUnknownText
    }

    Row(
        modifier = modifier
            .background(color = bgColor, shape = CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
