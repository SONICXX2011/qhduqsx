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
     * ========================================================
     * CHARACTER EVENT
     * ========================================================
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

    /*
     * ========================================================
     * CHARACTER OPEN
     * ========================================================
     */
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

    /*
     * ========================================================
     * PLAYER NAME
     * ========================================================
     *
     * Frida باید فقط در زمان Character
     * این متد را صدا بزند.
     */
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

    /*
     * ========================================================
     * CLEAR PLAYER NAME
     * ========================================================
     */
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

    /*
     * ========================================================
     * ROLE
     * ========================================================
     */
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

    /*
     * ========================================================
     * MONEY
     * ========================================================
     */
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

    /*
     * ========================================================
     * BACK MENU
     * ========================================================
     */
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

    /*
     * ========================================================
     * EXIT
     * ========================================================
     */
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

    /*
     * ========================================================
     * NOTIFICATION
     * ========================================================
     */
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