package br.com.meirelesefreitas.go.checkonline.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.meirelesefreitas.go.checkonline.ui.theme.BannerGradientEnd
import br.com.meirelesefreitas.go.checkonline.ui.theme.BannerGradientMiddle
import br.com.meirelesefreitas.go.checkonline.ui.theme.BannerGradientStart
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AnimatedGradientBanner(
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    title: String = "CheckOnline",
    subtitle: String = "Controle de Jornada & Checklist em Campo"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "softWaveAnimation")

    // Slow, soft harmonic wave phases
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase2"
    )

    val wavePhase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase3"
    )

    // Gentle breathing amplitude factor for natural organic feel
    val breathingAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAmplitude"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(245.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BannerGradientStart,
                        BannerGradientMiddle,
                        BannerGradientEnd
                    )
                )
            )
    ) {
        // Canvas with multi-layered soft liquid organic waves
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // --- Layer 1: Back Wave (Soft translucent deep green/teal tint) ---
            val path1 = Path()
            val baseHeight1 = height * 0.58f
            val amp1 = 20f * breathingAmplitude
            path1.moveTo(0f, height)
            path1.lineTo(0f, baseHeight1)
            var x = 0f
            val step = 6f
            while (x <= width) {
                val y = baseHeight1 + amp1 * sin((x / (width * 0.35f)) + wavePhase1)
                path1.lineTo(x, y)
                x += step
            }
            path1.lineTo(width, height)
            path1.close()

            drawPath(
                path = path1,
                color = Color.White.copy(alpha = 0.07f)
            )

            // --- Layer 2: Middle Wave (Gentle harmonic layer) ---
            val path2 = Path()
            val baseHeight2 = height * 0.68f
            val amp2 = 24f * (2f - breathingAmplitude)
            path2.moveTo(0f, height)
            path2.lineTo(0f, baseHeight2)
            x = 0f
            while (x <= width) {
                val y = baseHeight2 + amp2 * sin((x / (width * 0.28f)) - wavePhase2 + 1.2f)
                path2.lineTo(x, y)
                x += step
            }
            path2.lineTo(width, height)
            path2.close()

            drawPath(
                path = path2,
                color = Color(0xFFA7F3D0).copy(alpha = 0.12f)
            )

            // --- Layer 3: Foreground Wave (Crisp soft glowing crest) ---
            val path3 = Path()
            val crestPath = Path()
            val baseHeight3 = height * 0.76f
            val amp3 = 16f * breathingAmplitude
            path3.moveTo(0f, height)
            path3.lineTo(0f, baseHeight3)

            var isFirst = true
            x = 0f
            while (x <= width) {
                val y = baseHeight3 + amp3 * sin((x / (width * 0.22f)) + wavePhase3)
                path3.lineTo(x, y)
                if (isFirst) {
                    crestPath.moveTo(x, y)
                    isFirst = false
                } else {
                    crestPath.lineTo(x, y)
                }
                x += step
            }
            path3.lineTo(width, height)
            path3.close()

            // Fill foreground wave
            drawPath(
                path = path3,
                color = Color.White.copy(alpha = 0.14f)
            )

            // Subtle glowing stroke on crest
            drawPath(
                path = crestPath,
                color = Color.White.copy(alpha = 0.30f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        // Content overlay: Logo and Title
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showLogo) {
                // Smooth rounded container for "MF" Logo
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.96f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(
                            text = "MF",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BannerGradientStart,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}
