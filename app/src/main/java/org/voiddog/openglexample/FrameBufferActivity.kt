package org.voiddog.openglexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import java.lang.Exception

class FrameBufferActivity : AppCompatActivity() {

    lateinit var imgPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_buffer)
        imgPreview = findViewById(R.id.img_preview)
        findViewById<Button>(R.id.btn_render).setOnClickListener {
            startRender()
        }
    }

    private fun startRender() {
        Thread {
            val render = FrameBufferRender()
            try {
                if(!render.initEGLContext()) {
                    return@Thread
                }

                render.initGL()
                val bitmap = render.draw(200, 200)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    imgPreview.post {
                        imgPreview.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                render.destroy()
            }
        }.start()
    }
}