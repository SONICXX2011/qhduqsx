package com.example.gameui

import android.app.Activity
import android.os.Handler
import android.os.Looper

import java.util.concurrent.atomic.AtomicLong

object UnityGameUIBridge {

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    private val loginRequestCounter =
        AtomicLong(0L)

    // =========================================================
    // SHOW
    // =========================================================

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

    // =========================================================
    // HIDE
    // =========================================================

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

    // =========================================================
    // DESTROY
    // =========================================================

    @JvmStatic
    fun destroy() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.destroy()

        } else {

            mainHandler.post {

                GameMenu.destroy()
            }
        }
    }

    // =========================================================
    // GAME STATE
    // =========================================================

    /**
     * menu:
     *
     * 0 = MAIN
     * 1 = LAN
     * 2 = COMMUNITY
     * 3 = CHARACTER
     * 4 = SETTINGS
     * 5 = ABOUT
     *
     * networkActive:
     *
     * true  = داخل بازی/اتصال فعال
     * false = بدون اتصال فعال
     */
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

    // =========================================================
    // START GAME
    // =========================================================

    /**
     * فقط Event UI.
     *
     * اینجا هیچ NetworkManager،
     * Uri،
     * StartClient،
     * IP،
     * Port
     * وجود ندارد.
     */
    @JvmStatic
    fun startGame() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.onStartGameClicked()

        } else {

            mainHandler.post {

                GameMenu.onStartGameClicked()
            }
        }
    }

    // =========================================================
    // JOIN NOTIFICATION
    // =========================================================

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
    fun clearJoinNotification() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.clearJoinNotification()

        } else {

            mainHandler.post {

                GameMenu.clearJoinNotification()
            }
        }
    }

    // =========================================================
    // CHARACTER
    // =========================================================

    @JvmStatic
    fun openCharacter() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.openCharacterFromBridge()

        } else {

            mainHandler.post {

                GameMenu.openCharacterFromBridge()
            }
        }
    }

    // =========================================================
    // CHARACTER DATA
    // =========================================================

    /**
     * Frida می‌تواند اطلاعات Character را
     * از Unity گرفته و به Kotlin بدهد.
     */
    @JvmStatic
    fun setCharacterInfo(
        name: String?,
        role: String?,
        money: String?
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.setCharacterInfo(
                name,
                role,
                money
            )

        } else {

            mainHandler.post {

                GameMenu.setCharacterInfo(
                    name,
                    role,
                    money
                )
            }
        }
    }

    @JvmStatic
    fun clearCharacterInfo() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.clearCharacterInfo()

        } else {

            mainHandler.post {

                GameMenu.clearCharacterInfo()
            }
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    /**
     * Event فقط.
     *
     * Login خودش هیچ شبکه‌ای انجام نمی‌دهد.
     */
    @JvmStatic
    fun requestLogin() {

        loginRequestCounter.incrementAndGet()
    }

    /**
     * آخرین شماره درخواست Login.
     *
     * Frida می‌تواند این مقدار را Poll کند.
     */
    @JvmStatic
    fun getLoginRequestId(): Long {

        return loginRequestCounter.get()
    }

    // =========================================================
    // HELP
    // =========================================================

    @JvmStatic
    fun openHelp() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.openHelp()

        } else {

            mainHandler.post {

                GameMenu.openHelp()
            }
        }
    }

    @JvmStatic
    fun closeHelp() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.closeHelp()

        } else {

            mainHandler.post {

                GameMenu.closeHelp()
            }
        }
    }

    // =========================================================
    // CHARACTER -> MAIN
    // =========================================================

    /**
     * برای BackMenu واقعی بهتر است Frida
     * setGameState(0, false) را صدا بزند.
     *
     * این API فقط برای کنترل مستقیم UI است.
     */
    @JvmStatic
    fun closeCharacterToMain() {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            GameMenu.closeCharacterToMain()

        } else {

            mainHandler.post {

                GameMenu.closeCharacterToMain()
            }
        }
    }
}