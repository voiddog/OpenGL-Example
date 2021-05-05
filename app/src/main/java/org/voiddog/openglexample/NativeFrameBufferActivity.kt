package org.voiddog.openglexample

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Lifecycle

class NativeFrameBufferActivity : AppCompatActivity() {

  companion object {
    init {
      System.loadLibrary("native-lib")
    }
  }

  lateinit var imgPreview: ImageView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_native_frame_buffer)
    imgPreview = findViewById(R.id.img_preview)
    render()
  }

  fun render() {
    Thread {
      val pixels = draw(400, 400)
      if (pixels != null) {
        val bitmap = Bitmap.createBitmap(pixels, 400, 400, Bitmap.Config.ARGB_8888)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
          imgPreview.setImageBitmap(bitmap)
        }
      }
    }.start()
  }

  external fun draw(width: Int, height: Int): IntArray?
}