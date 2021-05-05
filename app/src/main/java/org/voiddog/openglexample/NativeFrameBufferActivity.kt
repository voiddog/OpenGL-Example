package org.voiddog.openglexample

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class NativeFrameBufferActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_native_frame_buffer)
  }

  external fun draw(width: Int, height: Int): Bitmap
}