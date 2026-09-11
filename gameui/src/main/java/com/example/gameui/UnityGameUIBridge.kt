package com.example.gameui

import android.app.Activity
import android.os.Handler
import android.os.Looper

object UnityGameUIBridge {

    private val mainHandler =
        Handler(Looper.getMainLooper())

    @JvmStatic
    fun show(activity: Activity) {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.show(activity)
        } else {
            mainHandler.post {
                GameMenu.show(activity)
            }
        }
    }

    @JvmStatic
    fun hide() {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.hide()
        } else {
            mainHandler.post {
                GameMenu.hide()
            }
        }
    }

    /**
     * Frida وضعیت Unity را اینجا می‌فرستد.
     *
     * 0 MAIN
     * 1 LAN
     * 2 COMMUNITY
     * 3 CHARACTER
     * 4 SETTINGS
     * 5 ABOUT
     */
    @JvmStatic
    fun setGameState(
        menu: Int,
        networkActive: Boolean
    ) {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.setGameState(
                menu,
                networkActive
            )
        } else {
            mainHandler.post {
                GameMenu.setGameState(
                    menu,
                    networkActive
                )
            }
        }
    }

    /**
     * Custom Start Game button.
     *
     * فقط event را به کد Kotlin می‌دهد.
     * منطق اتصال در Frida است.
     */
    @JvmStatic
    fun startGame() {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.onStartGameClicked()
        } else {
            mainHandler.post {
                GameMenu.onStartGameClicked()
            }
        }
    }

    /**
     * Frida این متد را hook می‌کند.
     *
     * Kotlin فقط signal می‌دهد.
     * اجرای StartClient در TypeScript است.
     */
    @JvmStatic
    fun requestStartGame() {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.onStartGameClicked()
        } else {
            mainHandler.post {
                GameMenu.onStartGameClicked()
            }
        }
    }

    /**
     * Custom Character button event.
     *
     * پیاده‌سازی Unity Button.Press() در Frida است.
     */
    @JvmStatic
    fun requestUnityCharacter() {
        // Intentionally empty.
        // Frida hooks this static method as a signal.
    }

    @JvmStatic
    fun showJoinNotification() {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.showJoinNotification()
        } else {
            mainHandler.post {
                GameMenu.showJoinNotification()
            }
        }
    }

    @JvmStatic
    fun openCharacter() {
        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            GameMenu.openCharacterFromBridge()
        } else {
            mainHandler.post {
                GameMenu.openCharacterFromBridge()
            }
        }
    }
}