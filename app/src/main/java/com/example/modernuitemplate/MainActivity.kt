package com.example.modernuitemplate

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import com.example.gameui.GameMenu

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(FrameLayout(this))

        GameMenu.show(this)
    }

    override fun onDestroy() {
        GameMenu.hide()
        super.onDestroy()
    }
}
