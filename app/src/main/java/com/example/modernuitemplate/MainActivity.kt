package com.example.modernuitemplate

import android.os.Bundle
import android.widget.FrameLayout

import androidx.activity.ComponentActivity

import com.example.gameui.UnityGameUIBridge

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        /*
         * Activity فقط Host است.
         *
         * هیچ منطق Unity یا Network
         * در این Activity وجود ندارد.
         */
        setContentView(
            FrameLayout(this)
        )

        /*
         * نمایش UI.
         */
        UnityGameUIBridge.show(
            this
        )
    }

    override fun onDestroy() {

        /*
         * Cleanup کامل UI.
         */
        UnityGameUIBridge.destroy()

        super.onDestroy()
    }
}