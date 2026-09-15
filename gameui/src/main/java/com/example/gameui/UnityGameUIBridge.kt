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

    /*
     * ========================================================
     * FIRST-LAUNCH WELCOME
     * ========================================================
     *
     * Frida این متد را در startup صدا می‌زند.
     * فقط یک‌بار (per install) نمایش داده می‌شود.
     */
    @JvmStatic
    fun showWelcomeOnce(
        activity: Activity
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.showWelcomeOnce(
                activity
            )

        } else {

            mainHandler.post {

                GameMenu.showWelcomeOnce(
                    activity
                )
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

    @JvmStatic
    fun startGame() {
    }

    @JvmStatic
    fun openCharacter() {
    }

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

    @JvmStatic
    fun setPlayerName(
        name: String?
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.setPlayerName(
                name
            )

        } else {

            mainHandler.post {

                GameMenu.setPlayerName(
                    name
                )
            }
        }
    }

    @JvmStatic
    fun clearPlayerName() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.clearPlayerName()

        } else {

            mainHandler.post {

                GameMenu.clearPlayerName()
            }
        }
    }

    @JvmStatic
    fun setPlayerRole(
        role: String?
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.setPlayerRole(
                role
            )

        } else {

            mainHandler.post {

                GameMenu.setPlayerRole(
                    role
                )
            }
        }
    }

    @JvmStatic
    fun setPlayerMoney(
        money: String?
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.setPlayerMoney(
                money
            )

        } else {

            mainHandler.post {

                GameMenu.setPlayerMoney(
                    money
                )
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
}