package com.example.gameui

import android.app.Activity
import android.os.Handler
import android.os.Looper

object UnityGameUIBridge {

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    @JvmStatic
    fun show(
        activity: Activity
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.show(
                activity
            )

        } else {

            mainHandler.post {
                GameMenu.show(
                    activity
                )
            }
        }
    }

    @JvmStatic
    fun hide() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.hide()

        } else {

            mainHandler.post {
                GameMenu.hide()
            }
        }
    }

    @JvmStatic
    fun setGameState(
        menu: Int,
        networkActive: Boolean
    ) {

        if (
            Looper.myLooper() ==
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

    /*
     * فقط signal برای Frida.
     *
     * هیچ NetworkManager یا StartClient
     * داخل Kotlin وجود ندارد.
     */
    @JvmStatic
    fun startGame() {
    }

    @JvmStatic
    fun showJoinNotification() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.showJoinNotification()

        } else {

            mainHandler.post {
                GameMenu.showJoinNotification()
            }
        }
    }

    /*
     * فقط signal برای Frida.
     *
     * خود Unity Character توسط Frida
     * مدیریت می‌شود.
     */
    @JvmStatic
    fun openCharacter() {
    }
}