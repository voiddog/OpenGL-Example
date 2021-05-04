package org.voiddog.openglexample

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

class SingleExampleActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var render: MyRender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_example)
        if (!supportGLES20()) {
            finish()
            return
        }

        glSurfaceView = findViewById(R.id.gl_view)

        // init gl surface
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        render = MyRender()
        glSurfaceView.setRenderer(render)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onPause() {
        glSurfaceView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onDestroy() {
        render.destroy()
        super.onDestroy()
    }

    private fun supportGLES20(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.deviceConfigurationInfo.reqGlEsVersion >= 0x20000
    }
}

private class MyRender : GLSurfaceView.Renderer {

    companion object {
        const val VERTEX_SHADER = """
      attribute vec4 vPosition;
      uniform mat4 uMVPMatrix;
      
      void main() {
        gl_Position = uMVPMatrix * vPosition;
      }
    """

        const val FRAGMENT_SHADER = """
      precision mediump float;
      
      void main() {
        gl_FragColor = vec4(1, 0, 0, 1);
      }
    """

        val VERTEX = floatArrayOf(
            0f, 1f, 0f,     // top
            -sqrt(3f) /2f, -0.5f, 0f, // bottom left
            sqrt(3f) /2f, -0.5f, 0f,    // bottom right
        )
    }

    private val vertexArrayBuffer = ByteBuffer.allocateDirect(VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTEX)

    init {
        vertexArrayBuffer.position(0)
    }

    var program: Int = -1
    var positionHandler: Int = -1
    var matrixHandler: Int = -1
    var mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 1f)

        program = GLES20.glCreateProgram()
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        GLES20.glUseProgram(program)

        positionHandler = GLES20.glGetAttribLocation(program, "vPosition")
        matrixHandler = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT,
            false, 12, vertexArrayBuffer)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        Matrix.perspectiveM(mvpMatrix, 0, 90f, width.toFloat() / height, 0.5f, 100f)
        Matrix.translateM(mvpMatrix, 0, 0f, 0f, -5f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 向 matrix 赋值
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }

    fun destroy() {}

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}