package com.example.gameui

import android.app.Activity
import android.os.Handler
import android.os.Looper

object UnityGameUIBridge {

    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun show(activity: Activity) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            GameMenu.show(activity)
        } else {
            mainHandler.post {
                GameMenu.show(activity)
            }
        }
    }

    @JvmStatic
    fun hide() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            GameMenu.hide()
        } else {
            mainHandler.post {
                GameMenu.hide()
            }
        }
    }

    @JvmStatic
    fun startGame() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            GameMenu.onStartGameClicked()
        } else {
            mainHandler.post {
                GameMenu.onStartGameClicked()
            }
        }
    }
}