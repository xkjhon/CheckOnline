package br.com.meirelesefreitas.go.checkonline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.meirelesefreitas.go.checkonline.ui.theme.ExpressiveShapes

fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF232724),
            Color(0xFF343A36),
            Color(0xFF232724)
        )
    } else {
        listOf(
            Color(0xFFE4ECE3),
            Color(0xFFF2F7F1),
            Color(0xFFE4ECE3)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    this
        .clip(shape)
        .background(brush)
}

@Composable
fun SkeletonItem(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val mod = if (width != null) modifier.width(width) else modifier.fillMaxWidth()
    Box(
        modifier = mod
            .height(height)
            .shimmerEffect(shape)
    )
}

@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SkeletonItem(width = 160.dp, height = 28.dp, shape = RoundedCornerShape(8.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonItem(width = 220.dp, height = 16.dp, shape = RoundedCornerShape(6.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shimmerEffect(CircleShape)
            )
        }

        // Banner 1: Clock Skeleton
        Card(
            shape = ExpressiveShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkeletonItem(width = 180.dp, height = 18.dp)
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonItem(width = 200.dp, height = 48.dp, shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonItem(width = 240.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(20.dp))
                SkeletonItem(height = 50.dp, shape = ExpressiveShapes.medium)
            }
        }

        // Banner 2: Pending days Skeleton
        Card(
            shape = ExpressiveShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).shimmerEffect(CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    SkeletonItem(width = 180.dp, height = 18.dp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                SkeletonItem(height = 14.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonItem(width = 240.dp, height = 12.dp)
            }
        }

        // Banner 3: Sync status Skeleton
        Card(
            shape = ExpressiveShapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(24.dp).shimmerEffect(CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        SkeletonItem(width = 160.dp, height = 16.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonItem(width = 120.dp, height = 12.dp)
                    }
                }
                SkeletonItem(width = 90.dp, height = 36.dp, shape = ExpressiveShapes.small)
            }
        }
    }
}

@Composable
fun HistoryScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SkeletonItem(width = 140.dp, height = 28.dp)
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonItem(width = 180.dp, height = 14.dp)
            }
            SkeletonItem(width = 110.dp, height = 40.dp, shape = ExpressiveShapes.medium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonItem(width = 80.dp, height = 32.dp, shape = ExpressiveShapes.small)
            SkeletonItem(width = 100.dp, height = 32.dp, shape = ExpressiveShapes.small)
            SkeletonItem(width = 80.dp, height = 32.dp, shape = ExpressiveShapes.small)
        }

        repeat(4) {
            Card(
                shape = ExpressiveShapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).shimmerEffect(CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            SkeletonItem(width = 90.dp, height = 16.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            SkeletonItem(width = 120.dp, height = 12.dp)
                        }
                    }
                    SkeletonItem(width = 60.dp, height = 24.dp, shape = ExpressiveShapes.small)
                }
            }
        }
    }
}
