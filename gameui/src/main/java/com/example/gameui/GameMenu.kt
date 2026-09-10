package com.example.gameui

import android.app.Activity
import android.graphics.Color
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.ComposeView

/**
 * Public entry point for the UI library.
 *
 * A host Activity can call GameMenu.show(activity) to add the Compose UI
 * to the Activity's content root.
 */
object GameMenu {

    private const val TAG = "GameMenu"

    @Volatile
    private var composeView: ComposeView? = null

    @JvmStatic
    fun show(activity: Activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { show(activity) }
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val root = activity.findViewById<ViewGroup>(android.R.id.content)
            ?: return

        if (composeView != null) {
            return
        }

        val view = ComposeView(activity).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setContent {
                GameMenuContent(
                    onStartGame = {
                        onStartGameClicked()
                    }
                )
            }
        }

        root.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        composeView = view
    }

    @JvmStatic
    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            composeView?.post { hide() }
            return
        }

        composeView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
            view.disposeComposition()
        }

        composeView = null
    }

    /**
     * Called by the Start Game button.
     * This stable public method is also a convenient hook point for diagnostics.
     */
    @JvmStatic
    fun onStartGameClicked() {
        Log.i(TAG, "START_GAME_CLICKED")
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
        Button(onClick = onStartGame) {
            Text("شروع بازی")
        }
    }
}
