/*
 * HyperOS-style 动态柔雾背景（Compose 1.6+）
 * compileSdk ≥ 31
 */

@file:Suppress("MagicNumber")

package com.release.startcommunity.view

import android.graphics.RuntimeShader
import android.graphics.Shader
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.release.startcommunity.tool.BackGLRender
import java.io.BufferedReader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ────────────── 公开入口 ────────────── */



@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HyperOSBackground(
    modifier: Modifier = Modifier,
    blurRadius: Float = 80f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val composeEffect = remember(blurRadius, density) {
        val px = with(density) { blurRadius.dp.toPx() }
        android.graphics.RenderEffect
            .createBlurEffect(px, px, Shader.TileMode.CLAMP)
            .asComposeRenderEffect()
    }

    Box(modifier.fillMaxSize()) {

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { renderEffect = composeEffect }
        ) {
            AnimatedCloudBlobs()
        }


        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/* ────────────── 动态云朵 ────────────── */

@Composable
private fun AnimatedCloudBlobs(
    modifier: Modifier = Modifier,
    baseColors: List<Color> = listOf(
        Color(0xFF0FF6E8),
        Color(0xFF06EEFF),
        Color(0xFF07E5FF),
        Color(0xFF06E8D9)
    )
) {
    /* 无限相位动画（缓慢漂移） */
    val transition = rememberInfiniteTransition(label = "clouds")
    val phaseA by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val phaseB by transition.animateFloat(
        initialValue = PI.toFloat(),
        targetValue  = 3 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    /* Canvas 必须提供 onDraw λ ↓ */
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        fun center(idx: Int, phase: Float) = Offset(
            w * (0.5f + (0.3f + 0.05f * idx) * cos(phase + idx)),
            h * (0.5f + (0.3f + 0.05f * idx) * sin(phase + idx))
        )

        baseColors.forEachIndexed { i, color ->
            val p  = if (i % 2 == 0) phaseA else phaseB
            val c  = center(i, p)
            val r  = w * (0.25f + 0.03f * i)

            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0f)),
                    center = c,
                    radius = r
                ),
                center = c,
                radius = r
            )
        }
    }
}