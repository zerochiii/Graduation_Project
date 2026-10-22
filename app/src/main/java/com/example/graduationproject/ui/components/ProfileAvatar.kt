package com.example.graduationproject.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.profile.AvatarCatalog
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_COLOR_ID
import java.io.File

@Composable
internal fun ProfileAvatar(
    avatarId: String,
    avatarColorId: String = DEFAULT_PROFILE_AVATAR_COLOR_ID,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onImageLoadError: (() -> Unit)? = null
) = ProfileAvatar(
    avatarSource = AvatarSource.Preset(avatarId),
    avatarColorId = avatarColorId,
    size = size,
    modifier = modifier,
    contentDescription = contentDescription,
    onImageLoadError = onImageLoadError
)

@Composable
internal fun ProfileAvatar(
    avatarSource: AvatarSource,
    avatarColorId: String = DEFAULT_PROFILE_AVATAR_COLOR_ID,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onImageLoadError: (() -> Unit)? = null
) {
    val presetId = (avatarSource as? AvatarSource.Preset)?.avatarId
    val avatar = AvatarCatalog.find(presetId)
    val avatarColor = AvatarCatalog.findColor(avatarColorId)
    Surface(
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription ?: when (avatarSource) {
                    is AvatarSource.Preset -> "${avatar.displayName}頭像"
                    is AvatarSource.LocalImage -> "自訂照片頭像"
                }
            },
        shape = CircleShape,
        color = avatarColor.color
    ) {
        when (avatarSource) {
            is AvatarSource.Preset -> PresetAvatarIcon(avatarId = avatarSource.avatarId, size = size)
            is AvatarSource.LocalImage -> {
                SubcomposeAsyncImage(
                    model = File(avatarSource.path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(size * 0.05f)
                        .clip(CircleShape),
                    loading = { PresetAvatarIcon(avatarId = null, size = size) },
                    error = {
                        androidx.compose.runtime.LaunchedEffect(avatarSource.path) {
                            onImageLoadError?.invoke()
                        }
                        PresetAvatarIcon(avatarId = null, size = size)
                    },
                    success = { SubcomposeAsyncImageContent() }
                )
            }
        }
    }
}

@Composable
private fun PresetAvatarIcon(avatarId: String?, size: Dp) {
    val avatar = AvatarCatalog.find(avatarId)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = avatar.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}
