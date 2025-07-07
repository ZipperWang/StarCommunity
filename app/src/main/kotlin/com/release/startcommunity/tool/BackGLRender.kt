package com.release.startcommunity.tool

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BackGLRender(private val context: Context) : GLSurfaceView.Renderer {

    private var shaderProgram: Int = 0
    private var startTime = SystemClock.uptimeMillis()

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        val vertexShaderCode = loadFromAssets("bg_vert.glsl") ?: return
        val fragmentShaderCode = loadFromAssets("bg_frag.glsl") ?: return

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        shaderProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(shaderProgram)

        // 可传递时间、分辨率等 uniform 变量
        val time = (SystemClock.uptimeMillis() - startTime) / 1000f
        val timeLocation = GLES20.glGetUniformLocation(shaderProgram, "iTime")
        GLES20.glUniform1f(timeLocation, time)

        val resolutionLocation = GLES20.glGetUniformLocation(shaderProgram, "iResolution")
        GLES20.glUniform2f(resolutionLocation, 1080f, 1920f)

        // 自定义绘制（画一个 full screen quad 等）
    }

    private fun loadFromAssets(filename: String): String? {
        return try {
            context.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("BackGLRender", "Asset load error: ${e.message}")
            null
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                Log.e("BackGLRender", "Shader compile error: $error")
                GLES20.glDeleteShader(shader)
            }
        }
    }
}