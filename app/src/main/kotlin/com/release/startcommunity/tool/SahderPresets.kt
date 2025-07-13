package com.release.startcommunity.tool


object ShaderPresets {
    data class ShaderPreset(
        val points: FloatArray,
        val colors: FloatArray,
        val bound: FloatArray,
        val translateY: Float,
        val alphaMulti: Float,
        val noiseScale: Float,
        val pointOffset: Float,
        val pointRadiusMulti: Float,
        val saturateOffset: Float,
        val lightOffset: Float,
        val alphaOffset: Float,
        val shadowColorMulti: Float,
        val shadowColorOffset: Float,
        val shadowOffset: Float,
        val shadowNoiseScale: Float
    )

    val lightPreset = ShaderPreset(
        points = floatArrayOf(
            0.67f, 0.42f, 1.0f,
            0.69f, 0.75f, 1.0f,
            0.14f, 0.71f, 1.0f,
            0.95f, 0.14f, 1.0f
        ),
        colors = floatArrayOf(
            0.57f, 0.76f, 0.98f, 1.0f,
            0.98f, 0.85f, 0.68f, 1.0f,
            0.98f, 0.75f, 0.93f, 1.0f,
            0.73f, 0.7f, 0.98f, 1.0f
        ),
        bound = floatArrayOf(0.0f, 0.4489f, 1.0f, 0.5511f),
        translateY = 0f,
        alphaMulti = 1.0f,
        noiseScale = 1.5f,
        pointOffset = 0.1f,
        pointRadiusMulti = 1.0f,
        saturateOffset = 0.2f,
        lightOffset = 0.1f,
        alphaOffset = 0.5f,
        shadowColorMulti = 0.3f,
        shadowColorOffset = 0.3f,
        shadowOffset = 0.01f,
        shadowNoiseScale = 5.0f
    )

    val darkPreset = lightPreset.copy(
        points = floatArrayOf(
            0.63f, 0.5f, 0.88f,
            0.69f, 0.75f, 0.8f,
            0.17f, 0.66f, 0.81f,
            0.14f, 0.24f, 0.72f
        ),
        colors = floatArrayOf(
            0.0f, 0.31f, 0.58f, 1.0f,
            0.53f, 0.29f, 0.15f, 1.0f,
            0.46f, 0.06f, 0.27f, 1.0f,
            0.16f, 0.12f, 0.45f, 1.0f
        ),
        lightOffset = -0.1f,
        saturateOffset = 0.2f
    )
}