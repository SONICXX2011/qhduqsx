package com.example.modernuitemplate

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.example.gameui.GameMenu

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            FrameLayout(this)
        )

        GameMenu.show(this)
    }

    override fun onDestroy() {
        GameMenu.hide()
        super.onDestroy()
    }
}