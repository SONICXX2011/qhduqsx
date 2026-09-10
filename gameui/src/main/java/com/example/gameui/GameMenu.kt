package com.example.gameui

import android.app.Activity
import android.graphics.Color
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

object GameMenu {

    private const val TAG = "GameMenu"

    @Volatile
    private var composeView: ComposeView? = null

    /**
     * نمایش منوی UI روی Activity
     */
    @JvmStatic
    fun show(activity: Activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread {
                show(activity)
            }
            return
        }

        if (activity.isFinishing) {
            Log.w(TAG, "show(): activity is finishing")
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed) {
                Log.w(TAG, "show(): activity is destroyed")
                return
            }
        }

        // این نسخه برای Compose نیاز دارد Activity از ComponentActivity باشد.
        if (activity !is ComponentActivity) {
            Log.e(
                TAG,
                "show(): Activity must extend ComponentActivity"
            )
            return
        }

        val root = activity.findViewById<ViewGroup>(android.R.id.content)

        if (root == null) {
            Log.e(TAG, "show(): content root not found")
            return
        }

        // اگر قبلاً نمایش داده شده، دوباره اضافه نکن.
        if (composeView != null) {
            Log.d(TAG, "show(): GameMenu is already visible")
            return
        }

        val view = ComposeView(activity)

        view.setBackgroundColor(Color.TRANSPARENT)

        view.setContent {
            GameMenuContent(
                onStartGame = {
                    onStartGameClicked()
                }
            )
        }

        root.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        composeView = view

        Log.i(TAG, "GameMenu shown")
    }

    /**
     * مخفی کردن منو
     */
    @JvmStatic
    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            composeView?.post {
                hide()
            }
            return
        }

        val view = composeView ?: return

        val parent = view.parent

        if (parent is ViewGroup) {
            parent.removeView(view)
        }

        view.disposeComposition()

        composeView = null

        Log.i(TAG, "GameMenu hidden")
    }

    /**
     * رویداد کلیک دکمه شروع بازی
     */
    @JvmStatic
    fun onStartGameClicked() {
        Log.i(TAG, "START_GAME_CLICKED")
    }

    /**
     * بررسی اینکه منو در حال حاضر نمایش داده شده یا نه
     */
    @JvmStatic
    fun isVisible(): Boolean {
        return composeView != null
    }
}

@Composable
private fun GameMenuContent(
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Transparent)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onStartGame
        ) {
            Text(
                text = "شروع بازی"
            )
        }
    }
}