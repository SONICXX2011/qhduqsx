package com.example.gameui

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

object GameMenu {

    private const val TAG = "GameMenu"

    @Volatile
    private var composeView: ComposeView? = null

    @Volatile
    private var lifecycleOwner: GameLifecycleOwner? = null

    @JvmStatic
    fun show(activity: Activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread {
                show(activity)
            }
            return
        }

        if (activity.isFinishing) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed) {
                return
            }
        }

        val root = activity.findViewById<ViewGroup>(android.R.id.content)
            ?: return

        if (composeView != null) {
            return
        }

        try {
            val owner = GameLifecycleOwner()

            val view = ComposeView(activity).apply {
                setBackgroundColor(Color.TRANSPARENT)

                // ComposeView lifecycle owner
                setViewTreeLifecycleOwner(owner)

                // ComposeView saved-state owner
                setViewTreeSavedStateRegistryOwner(owner)

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

            owner.resume()

            lifecycleOwner = owner
            composeView = view

            Log.i(TAG, "GameMenu shown")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to show GameMenu", t)
        }
    }

    @JvmStatic
    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            composeView?.post {
                hide()
            }
            return
        }

        try {
            lifecycleOwner?.destroy()

            composeView?.let { view ->
                (view.parent as? ViewGroup)?.removeView(view)
                view.disposeComposition()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to hide GameMenu", t)
        }

        composeView = null
        lifecycleOwner = null

        Log.i(TAG, "GameMenu hidden")
    }

    @JvmStatic
    fun onStartGameClicked() {
        Log.i(TAG, "START_GAME_CLICKED")
    }
}

private class GameLifecycleOwner :
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val savedStateController =
        SavedStateRegistryController.create(this)

    init {
        savedStateController.performAttach()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    fun resume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onStartGame
            ) {
                Text("شروع بازی")
            }
        }
    }
}