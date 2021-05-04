package org.voiddog.openglexample

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

private val TAG = "FrameBufferRender"


class FrameBufferRender {

    private val vertexShader = """
        #version 300 es
        
        layout(location = 0) in vec2 vPos;
        uniform mat4 uMatrix;
        
        void main() {
            gl_Position = uMatrix * vec4(vPos.x, vPos.y, 0.0, 1.0);
        }
    """.trimIndent()

    private val fragmentShader = """
        #version 300 es
        precision mediump float;
        
        out vec4 fragColor;
        
        void main() {
            fragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
    """.trimIndent()

    fun initEGLContext(): Boolean {
        if (EGL14.eglGetCurrentContext() != EGL14.EGL_NO_CONTEXT) {
            Log.e(TAG, "EGL has already init.")
            return false
        }
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "Unable to open connection to local window system.")
            return false
        }

        val major = intArrayOf(0)
        val minor = intArrayOf(0)
        if (!EGL14.eglInitialize(display, major, 0, minor, 0)) {
            Log.e(TAG, "Unable to initialize EGL")
            return false
        }

        val config = arrayOf<EGLConfig?>(null)
        val numConfig = intArrayOf(0)
        val eglAttr = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        )
        if (!EGL14.eglChooseConfig(display, eglAttr, 0, config, 0,
                1, numConfig, 0)) {
            Log.e(TAG, "some config is wrong.")
            return false
        }

        val surface = EGL14.eglCreatePbufferSurface(display, config[0], null, 0)
        if (surface == EGL14.EGL_NO_SURFACE) {
            Log.e(TAG, "Can not create pbuffer surface")
            return false
        }

        val ctxAttr = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL14.EGL_NONE
        );
        val context = EGL14.eglCreateContext(display, config[0], EGL14.EGL_NO_CONTEXT,
            ctxAttr, 0)
        if (context == EGL14.EGL_NO_CONTEXT) {
            val error = EGL14.eglGetError()
            Log.e(TAG, "EGL error: $error")
            return false
        }

        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
            Log.e(TAG, "Unable to eglMake current");
            return false
        }

        Log.i(TAG, "EGL context init success.")
        return true
    }

    fun initGL() {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        // init VBO and VAO
        GLES30.glGenBuffers(1, VBO, 0)
        checkGLError("gen VBO/VAO")

        program = GLES30.glCreateProgram()
        // compile link shader
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShader)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader)
        if (vertexShader == -1 || fragmentShader == -1) {
            throw IllegalStateException("Shader compile error")
        }

        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        val linked = intArrayOf(0)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            throw IllegalStateException("Program link error")
        }

        matrixLocation = GLES30.glGetUniformLocation(program, "uMatrix")
        if (matrixLocation == -1) {
            throw IllegalStateException("No uniform matrix found.")
        }

        // init FBO
        GLES30.glGenFramebuffers(1, FBO, 0)
    }

    private val VBO = intArrayOf(-1)
    private val FBO = intArrayOf(-1)
    private var program = -1
    private var matrixLocation = -1

    fun draw(width: Int, height: Int):Bitmap {
        // 刷入 顶点数据
        val vertex = floatArrayOf(
            0f, height.toFloat(),
            0f, 0f,
            width.toFloat(), 0f
        )
        val vertexBuffer = ByteBuffer.allocateDirect(vertex.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex)
        vertexBuffer.position(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertex.size * 4,
            vertexBuffer, GLES30.GL_DYNAMIC_DRAW)
        // index: 0 表示顶点属性 id，shader 已经指定了是 0
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false,
            0, 0)
        GLES30.glEnableVertexAttribArray(0)

        val textureCanvas = intArrayOf(-1)
        try {
            GLES30.glGenTextures(1, textureCanvas, 0)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureCanvas[0])
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_REPEAT)
            checkGLError("Bind texture error")

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, FBO[0])
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, textureCanvas[0], 0)

            val proj = floatArrayOf(1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f)
            Matrix.orthoM(proj, 0, 0f, width.toFloat(), 0f,
                height.toFloat(), 0f, 100f)

            // draw
            GLES30.glViewport(0, 0, width, height)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

            // read pixels
            val bitmapBuffer = IntArray(width * height)
            val pixelBuffer = IntBuffer.wrap(bitmapBuffer)
            pixelBuffer.position(0)
            GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE, pixelBuffer)

            return Bitmap.createBitmap(bitmapBuffer, width, height, Bitmap.Config.ARGB_8888)
        } finally {
            if (textureCanvas[0] != -1) {
                GLES30.glDeleteTextures(1, textureCanvas, 0)
            }
        }
    }

    fun destroy() {
        if (VBO[0] != -1) {
            GLES30.glDeleteBuffers(1, VBO, 0)
        }
        if (FBO[0] != -1) {
            GLES30.glDeleteFramebuffers(1, FBO, 0)
        }
        if (program != -1) {
            GLES30.glDeleteProgram(program)
        }
    }

    private fun checkGLError(msg: String) {
        val errorCode = GLES30.glGetError()
        if (errorCode != GLES30.GL_NO_ERROR) {
            throw IllegalStateException("GL error($errorCode) for $msg")
        }
    }
}