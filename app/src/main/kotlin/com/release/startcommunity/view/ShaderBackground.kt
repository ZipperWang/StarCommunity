package com.release.startcommunity.view


import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.release.startcommunity.R
import com.release.startcommunity.tool.ShaderPresets.darkPreset
import com.release.startcommunity.tool.ShaderPresets.lightPreset
import java.io.BufferedReader
import java.io.InputStreamReader

@RequiresApi(Build.VERSION_CODES.TIRAMISU)//找了半天原来是Android13的API，借鉴的时候注意这个
@Composable
fun ShaderBackground(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val preset = if (isDark) darkPreset else lightPreset

    val shaderSource = remember {
        context.resources.openRawResource(R.raw.bg_frag).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }
    }

    val shader = remember { RuntimeShader(shaderSource) }

    val animTime by rememberInfiniteTransition(label = "bgAnim").animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.drawWithCache {
            onDrawBehind {
                shader.setFloatUniform("uAnimTime", animTime *0.05f)
                shader.setFloatUniform("uResolution", size.width, size.height)
                shader.setFloatUniform("uPoints", preset.points)
                shader.setFloatUniform("uColors", preset.colors)
                shader.setFloatUniform("uTranslateY", preset.translateY)
                shader.setFloatUniform("uBound", preset.bound)
                shader.setFloatUniform("uAlphaMulti", preset.alphaMulti)
                shader.setFloatUniform("uNoiseScale", preset.noiseScale)
                shader.setFloatUniform("uPointOffset", preset.pointOffset)
                shader.setFloatUniform("uPointRadiusMulti", preset.pointRadiusMulti)
                shader.setFloatUniform("uSaturateOffset", preset.saturateOffset)
                shader.setFloatUniform("uLightOffset", preset.lightOffset)
                shader.setFloatUniform("uAlphaOffset", preset.alphaOffset)
                shader.setFloatUniform("uShadowColorMulti", preset.shadowColorMulti)
                shader.setFloatUniform("uShadowColorOffset", preset.shadowColorOffset)
                shader.setFloatUniform("uShadowOffset", preset.shadowOffset)
                shader.setFloatUniform("uShadowNoiseScale", preset.shadowNoiseScale)

                drawRect(
                    brush = ShaderBrush(shader),
                    size = size,
                    style = Fill
                )
            }
        }
    )
}