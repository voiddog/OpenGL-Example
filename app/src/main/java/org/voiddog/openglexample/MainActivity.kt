package org.voiddog.openglexample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_single_example).setOnClickListener {
            Intent(this, SingleExampleActivity::class.java).let { intent ->
                startActivity(intent)
            }
        }
        findViewById<Button>(R.id.btn_frame_buffer).setOnClickListener {
            Intent(this, FrameBufferActivity::class.java).let { intent ->
                startActivity(intent)
            }
        }
    }
}