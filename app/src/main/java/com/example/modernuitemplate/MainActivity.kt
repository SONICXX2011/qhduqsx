package com.example.modernuitemplate

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.example.gameui.GameMenu

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            FrameLayout(this)
        )

        GameMenu.show(
            this
        )
    }

    override fun onDestroy() {

        /*
         * فقط UI را hide می‌کنیم.
         *
         * LifecycleOwner داخلی GameMenu
         * توسط خود GameMenu مدیریت می‌شود.
         */
        GameMenu.hide()

        super.onDestroy()
    }
}