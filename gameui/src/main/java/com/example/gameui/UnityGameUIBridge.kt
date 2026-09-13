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
     * Frida intercepts this.
     */
    @JvmStatic
    fun startGame() {
    }

    /*
     * Frida intercepts this.
     */
    @JvmStatic
    fun openCharacter() {
    }

    /*
     * Frida can call these directly if needed.
     */
    @JvmStatic
    fun onCharacterEvent() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.onCharacterEvent()

        } else {

            mainHandler.post {
                GameMenu.onCharacterEvent()
            }
        }
    }

    @JvmStatic
    fun onBackMenuEvent() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.onBackMenuEvent()

        } else {

            mainHandler.post {
                GameMenu.onBackMenuEvent()
            }
        }
    }

    @JvmStatic
    fun onExitEvent() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.onExitEvent()

        } else {

            mainHandler.post {
                GameMenu.onExitEvent()
            }
        }
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

    @JvmStatic
    fun openCharacterFromBridge() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.onCharacterEvent()

        } else {

            mainHandler.post {
                GameMenu.onCharacterEvent()
            }
        }
    }
}