package org.voiddog.openglexample

import android.opengl.GLES30
import android.util.Log

private val TAG = "GLUtils"

fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES30.glCreateShader(type)
    if (shader == 0) {
        Log.e(TAG, "can not create shader")
        return -1
    }

    GLES30.glShaderSource(shader, shaderCode)
    GLES30.glCompileShader(shader)
    val compiled = intArrayOf(0)
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        Log.e(TAG, "compile shader error")
        return -1;
    }

    return shader
}