package com.example.gameui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner

import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GameMenu {

    private const val TAG = "GameMenu"

    /*
     * ========================================================
     * GAME EDITOR LINK
     * ========================================================
     */
    private const val GAME_EDITOR_URL =
        "https://rubika.ir/SONICSELFPV"

    /*
     * ========================================================
     * WELCOME / FIRST LAUNCH
     * ========================================================
     */
    private const val WELCOME_PREFS =
        "gameui_welcome_prefs"

    private const val WELCOME_SHOWN_KEY =
        "welcome_message_confirmed"

    private const val WELCOME_SITE_URL =
        "https://lacrimesonline.com/"

    private const val WELCOME_YOUTUBE_URL =
        "https://youtube.com/@mohammadalizade"

    private const val WELCOME_DISCORD_URL =
        "https://discord.gg/aQhGqHSc3W"

    /*
     * ========================================================
     * PRE-ALLOCATED COLORS
     * ========================================================
     *
     * Compose Color یک value class است، ولی برای پرهیز از
     * هرگونه ابهام و کاهش تخصیص در recompose های مکرر،
     * رنگ‌های پرتکرار را یک‌بار می‌سازیم.
     */
    private val ACCENT_GREEN =
        ComposeColor(0xFF58E59A)

    private val ACCENT_GOLD =
        ComposeColor(0xFFF0C85C)

    private val ACCENT_BLUE =
        ComposeColor(0xFF72B8FF)

    private val TEXT_SOFT =
        ComposeColor(0xFFD1D9D5)

    private val TEXT_MUTED =
        ComposeColor(0xFFB7C5BE)

    private val TEXT_BRIGHT =
        ComposeColor(0xFFE8EFEB)

    private val BTN_BG =
        ComposeColor(0x33111A16)

    private val BTN_BG_LIGHT =
        ComposeColor(0x22111A16)

    private val PANEL_BG =
        ComposeColor(0xF218201C)

    private val PANEL_BORDER =
        ComposeColor(0x6658E59A)

    private val INFO_BORDER =
        ComposeColor(0x3358E59A)

    private val INFO_BG =
        ComposeColor(0x12111A16)

    private val SCRIM =
        ComposeColor(0xE6050907)

    private val CONFIRM_BG =
        ComposeColor(0x4458E59A)

    private val CONFIRM_BORDER =
        ComposeColor(0xFF58E59A)

    private val mainHandler =
        Handler(Looper.getMainLooper())

    @Volatile
    private var currentGameMenu = -1

    @Volatile
    private var networkActive = false

    private var visible by
        mutableStateOf(false)

    private var currentPage by
        mutableStateOf(Page.MAIN)

    private var characterLocked by
        mutableStateOf(false)

    /*
     * فقط Exit واقعی این را true می‌کند.
     */
    private var exitWaiting by
        mutableStateOf(false)

    private var notificationText by
        mutableStateOf<String?>(null)

    private var notificationToken = 0L

    /*
     * ========================================================
     * PLAYER INFO
     * ========================================================
     */
    private var currentPlayerName by
        mutableStateOf("Null")

    private var currentPlayerRole by
        mutableStateOf("Null")

    private var currentPlayerMoney by
        mutableStateOf("Null")

    private var composeView: ComposeView? = null
    private var lifecycleOwner: GameLifecycleOwner? = null

    /*
     * ========================================================
     * WELCOME STATE
     * ========================================================
     */
    private var welcomeVisible by
        mutableStateOf(false)

    private var welcomePreferences: SharedPreferences? =
        null

    private enum class Page {
        MAIN,
        CHARACTER,
        LOGIN,
        HELP
    }

    private enum class Accent {
        GREEN,
        GOLD,
        BLUE
    }

    /*
     * ========================================================
     * SHOW
     * ========================================================
     */
    @JvmStatic
    fun show(activity: Activity) {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                show(activity)
            }
            return
        }

        try {

            val decorView =
                activity.window?.decorView
                    as? ViewGroup
                    ?: return

            val existing =
                composeView

            if (existing != null) {

                lifecycleOwner?.let { owner ->

                    decorView
                        .setViewTreeLifecycleOwner(
                            owner
                        )

                    decorView
                        .setViewTreeSavedStateRegistryOwner(
                            owner
                        )

                    existing
                        .setViewTreeLifecycleOwner(
                            owner
                        )

                    existing
                        .setViewTreeSavedStateRegistryOwner(
                            owner
                        )
                }

                if (
                    existing.parent == null
                ) {
                    decorView.addView(
                        existing
                    )
                }

                updateVisibility()
                return
            }

            val owner =
                GameLifecycleOwner()

            owner.attachAndRestore()

            lifecycleOwner =
                owner

            decorView
                .setViewTreeLifecycleOwner(
                    owner
                )

            decorView
                .setViewTreeSavedStateRegistryOwner(
                    owner
                )

            val view =
                ComposeView(activity)

            view
                .setViewTreeLifecycleOwner(
                    owner
                )

            view
                .setViewTreeSavedStateRegistryOwner(
                    owner
                )

            view.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

            view.visibility =
                View.GONE

            view.alpha =
                1f

            view.setContent {
                GameMenuRoot()
            }

            composeView =
                view

            decorView.addView(
                view
            )

            updateVisibility()

            Log.d(
                TAG,
                "ComposeView created"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "show failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * HIDE
     * ========================================================
     */
    @JvmStatic
    fun hide() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                hide()
            }
            return
        }

        try {

            visible =
                false

            composeView?.visibility =
                View.GONE

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "hide failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * SHOW WELCOME ONCE
     * ========================================================
     *
     * Frida این تابع را بلافاصله بعد از load شدن صدا می‌زند.
     *
     * فقط بعد از زدن دکمه "باشه، متوجه شدم" مقدار
     * SharedPreferences ثبت می‌شود.
     */
    @JvmStatic
    fun showWelcomeOnce(
        activity: Activity
    ) {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                showWelcomeOnce(
                    activity
                )
            }
            return
        }

        try {

            val prefs =
                activity.getSharedPreferences(
                    WELCOME_PREFS,
                    Activity.MODE_PRIVATE
                )

            welcomePreferences =
                prefs

            if (
                prefs.getBoolean(
                    WELCOME_SHOWN_KEY,
                    false
                )
            ) {
                return
            }

            show(
                activity
            )

            welcomeVisible =
                true

            updateVisibility()

            Log.d(
                TAG,
                "WELCOME MESSAGE SHOWN"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "showWelcomeOnce failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * WELCOME CONFIRM
     * ========================================================
     */
    private fun confirmWelcomeMessage() {

        try {

            welcomePreferences
                ?.edit()
                ?.putBoolean(
                    WELCOME_SHOWN_KEY,
                    true
                )
                ?.apply()

            welcomeVisible =
                false

            updateVisibility()

            Log.d(
                TAG,
                "WELCOME MESSAGE CONFIRMED"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "confirmWelcomeMessage failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * EXTERNAL LINK
     * ========================================================
     */
    private fun openExternalLink(
        url: String
    ) {

        try {

            val view =
                composeView
                    ?: return

            val context =
                view.context

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        url
                    )
                )

            if (
                context !is Activity
            ) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

            context.startActivity(
                intent
            )

            Log.d(
                TAG,
                "EXTERNAL LINK OPENED -> $url"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "openExternalLink failed -> $url",
                t
            )
        }
    }

    /*
     * ========================================================
     * GAME STATE
     * ========================================================
     */
    @JvmStatic
    fun setGameState(
        menu: Int,
        active: Boolean
    ) {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                setGameState(
                    menu,
                    active
                )
            }
            return
        }

        try {

            currentGameMenu =
                menu

            networkActive =
                active

            if (active) {

                visible =
                    false

                updateVisibility()

                return
            }

            if (exitWaiting) {

                if (
                    menu == 0 &&
                    !active
                ) {

                    exitWaiting =
                        false

                    characterLocked =
                        false

                    currentPage =
                        Page.MAIN

                    visible =
                        true

                    updateVisibility()

                    Log.d(
                        TAG,
                        "EXIT RESTORE ACCEPTED -> MAIN UI VISIBLE"
                    )

                } else {

                    visible =
                        false

                    updateVisibility()
                }

                return
            }

            if (characterLocked) {

                currentPage =
                    Page.CHARACTER

                visible =
                    true

                updateVisibility()

                return
            }

            if (menu == 0) {

                currentPage =
                    Page.MAIN

                visible =
                    true

                updateVisibility()

                return
            }

            if (menu == 3) {

                characterLocked =
                    true

                currentPage =
                    Page.CHARACTER

                visible =
                    true

                updateVisibility()

                return
            }

            visible =
                false

            updateVisibility()

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "setGameState failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * CHARACTER EVENT
     * ========================================================
     */
    @JvmStatic
    fun onCharacterEvent() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                onCharacterEvent()
            }
            return
        }

        try {

            exitWaiting =
                false

            characterLocked =
                true

            currentPage =
                Page.CHARACTER

            visible =
                true

            updateVisibility()

            Log.d(
                TAG,
                "CHARACTER UI LOCKED"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "onCharacterEvent failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * PLAYER NAME FROM FRIDA
     * ========================================================
     */
    @JvmStatic
    fun setPlayerName(
        name: String?
    ) {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                setPlayerName(
                    name
                )
            }
            return
        }

        try {

            val cleanName =
                name
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: "Null"

            if (
                currentPlayerName != cleanName
            ) {

                currentPlayerName =
                    cleanName

                Log.d(
                    TAG,
                    "PLAYER NAME UPDATED -> $currentPlayerName"
                )
            }

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "setPlayerName failed",
                t
            )
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
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                clearPlayerName()
            }
            return
        }

        try {

            currentPlayerName =
                "Null"

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "clearPlayerName failed",
                t
            )
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
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                setPlayerRole(
                    role
                )
            }
            return
        }

        try {

            currentPlayerRole =
                role
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: "Null"

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "setPlayerRole failed",
                t
            )
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
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                setPlayerMoney(
                    money
                )
            }
            return
        }

        try {

            currentPlayerMoney =
                money
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: "Null"

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "setPlayerMoney failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * BACK MENU EVENT
     * ========================================================
     */
    @JvmStatic
    fun onBackMenuEvent() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                onBackMenuEvent()
            }
            return
        }

        try {

            characterLocked =
                false

            exitWaiting =
                false

            currentPage =
                Page.MAIN

            visible =
                !networkActive

            updateVisibility()

            Log.d(
                TAG,
                "BACK MENU -> MAIN"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "onBackMenuEvent failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * EXIT EVENT
     * ========================================================
     */
    @JvmStatic
    fun onExitEvent() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                onExitEvent()
            }
            return
        }

        try {

            characterLocked =
                false

            exitWaiting =
                true

            visible =
                false

            updateVisibility()

            Log.d(
                TAG,
                "EXIT -> WAITING FOR REAL MAIN"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "onExitEvent failed",
                t
            )
        }
    }

    /*
     * ========================================================
     * START GAME
     * ========================================================
     */
    @JvmStatic
    fun onStartGameClicked() {

        UnityGameUIBridge.startGame()
    }

    /*
     * ========================================================
     * CHARACTER BUTTON
     * ========================================================
     */
    @JvmStatic
    fun openCharacter() {

        UnityGameUIBridge.openCharacter()
    }

    @JvmStatic
    fun openCharacterFromBridge() {

        onCharacterEvent()
    }

    /*
     * ========================================================
     * GAME EDITOR LINK
     * ========================================================
     */
    private fun openGameEditorLink() {

        try {

            val view =
                composeView
                    ?: return

            val context =
                view.context

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        GAME_EDITOR_URL
                    )
                )

            if (
                context !is Activity
            ) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

            context.startActivity(
                intent
            )

            Log.d(
                TAG,
                "GAME EDITOR LINK OPENED"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "openGameEditorLink failed",
                t
            )
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
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {
            mainHandler.post {
                showJoinNotification()
            }
            return
        }

        notificationToken++

        val token =
            notificationToken

        notificationText =
            "درحال پیوستن به سرور..."

        mainHandler.postDelayed({

            if (
                notificationToken ==
                token
            ) {

                notificationText =
                    null
            }

        }, 3000L)
    }

    /*
     * ========================================================
     * VISIBILITY
     * ========================================================
     */
    private fun updateVisibility() {

        val view =
            composeView
                ?: return

        view.alpha =
            1f

        view.visibility =
            if (
                welcomeVisible ||
                (visible && !networkActive)
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }
    }

    /*
     * ========================================================
     * ROOT
     * ========================================================
     */
    @Composable
    private fun GameMenuRoot() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            /*
             * =================================================
             * MAIN GAME UI
             * =================================================
             *
             * این بخش وقتی Welcome باز است اصلاً compose
             * نمی‌شود. این باعث حذف کامل لگ می‌شود چون
             * دکمه‌های سنگین Main/Character زیر Welcome
             * دیگر رندر نمی‌شوند.
             */
            if (
                visible &&
                !networkActive &&
                !welcomeVisible
            ) {

                when (
                    currentPage
                ) {

                    Page.MAIN ->
                        MainPage()

                    Page.CHARACTER ->
                        CharacterPage()

                    Page.LOGIN ->
                        LoginPage()

                    Page.HELP ->
                        HelpPage()
                }
            }

            notificationText?.let { text ->

                JoinNotification(
                    text = text
                )
            }
        }

        /*
         * =================================================
         * FIRST-LAUNCH WELCOME
         * =================================================
         *
         * خارج از Box اصلی و در یک key مستقل،
         * تا recompose آن به‌صورت مستقل مدیریت شود.
         */
        if (
            welcomeVisible
        ) {

            key(
                welcomeVisible
            ) {

                WelcomeOverlay()
            }
        }
    }

    /*
     * ========================================================
     * MAIN PAGE
     * ========================================================
     */
    @Composable
    private fun MainPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            GlassButton(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .offset(
                            x = 18.dp
                        )
                        .padding(
                            top = 22.dp
                        )
                        .width(
                            205.dp
                        ),

                text =
                    "فرد ادیت کننده گیم",

                accent =
                    Accent.GREEN,

                onClick = {

                    openGameEditorLink()
                }
            )

            Row(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            bottom = 38.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                GlassButton(
                    modifier =
                        Modifier.width(
                            118.dp
                        ),

                    text =
                        "Character",

                    accent =
                        Accent.GOLD,

                    onClick = {

                        openCharacter()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )

                GlassButton(
                    modifier =
                        Modifier.width(
                            138.dp
                        ),

                    text =
                        "Start Game",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        onStartGameClicked()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )

                GlassButton(
                    modifier =
                        Modifier.width(
                            105.dp
                        ),

                    text =
                        "?  Help",

                    accent =
                        Accent.BLUE,

                    onClick = {

                        currentPage =
                            Page.HELP
                    }
                )
            }
        }
    }

    /*
     * ========================================================
     * CHARACTER PAGE
     * ========================================================
     */
    @Composable
    private fun CharacterPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 28.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                CharacterInfoText(
                    title =
                        "Name",

                    value =
                        currentPlayerName
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                CharacterInfoText(
                    title =
                        "Role",

                    value =
                        currentPlayerRole
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                CharacterInfoText(
                    title =
                        "Money",

                    value =
                        currentPlayerMoney
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                CharacterInfoText(
                    title =
                        "Iran Time",

                    value =
                        getIranTime()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                GlassButton(
                    modifier =
                        Modifier.width(
                            190.dp
                        ),

                    text =
                        "Login / Register",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        currentPage =
                            Page.LOGIN
                    }
                )
            }
        }
    }

    /*
     * ========================================================
     * CHARACTER INFO TEXT
     * ========================================================
     */
    @Composable
    private fun CharacterInfoText(
        title: String,
        value: String
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,

                        color =
                            INFO_BORDER,

                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    )
                    .background(
                        color =
                            INFO_BG,

                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    title,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    TEXT_SOFT
            )

            Text(
                text =
                    value.ifBlank {
                        "Null"
                    },

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    ComposeColor.White
            )
        }
    }

    /*
     * ========================================================
     * LOGIN PAGE
     * ========================================================
     */
    @Composable
    private fun LoginPage() {

        var password by
            remember {
                mutableStateOf("")
            }

        var confirmPassword by
            remember {
                mutableStateOf("")
            }

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 28.dp
                        )
                        .fillMaxWidth(),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "Login / Register",

                    fontSize =
                        21.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ComposeColor.White
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                OutlinedTextField(
                    value =
                        password,

                    onValueChange = {
                        password = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Password"
                        )
                    },

                    visualTransformation =
                        PasswordVisualTransformation()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                OutlinedTextField(
                    value =
                        confirmPassword,

                    onValueChange = {
                        confirmPassword =
                            it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Confirm Password"
                        )
                    },

                    visualTransformation =
                        PasswordVisualTransformation()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                GlassButton(
                    modifier =
                        Modifier.fillMaxWidth(),

                    text =
                        "Continue",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        Log.d(
                            TAG,
                            "LOGIN_CONTINUE_CLICKED"
                        )
                    }
                )
            }
        }
    }

    /*
     * ========================================================
     * HELP
     * ========================================================
     */
    @Composable
    private fun HelpPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            GlassSmallButton(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            top = 28.dp,
                            end = 18.dp
                        ),

                text =
                    "X",

                accent =
                    Accent.BLUE,

                onClick = {

                    currentPage =
                        Page.MAIN
                }
            )

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 28.dp
                        ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "Help",

                    fontSize =
                        25.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ComposeColor.White
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        "Help system will be available soon.",

                    fontSize =
                        14.sp,

                    color =
                        TEXT_MUTED
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                GlassButton(
                    modifier =
                        Modifier.fillMaxWidth(),

                    text =
                        "Close",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        currentPage =
                            Page.MAIN
                    }
                )
            }
        }
    }

    /*
     * ========================================================
     * GLASS BUTTON
     * ========================================================
     */
    @Composable
    private fun GlassButton(
        modifier: Modifier = Modifier,
        text: String,
        accent: Accent,
        onClick: () -> Unit
    ) {

        val accentColor =
            when (accent) {

                Accent.GREEN ->
                    ACCENT_GREEN

                Accent.GOLD ->
                    ACCENT_GOLD

                Accent.BLUE ->
                    ACCENT_BLUE
            }

        OutlinedButton(
            onClick =
                onClick,

            modifier =
                modifier.height(
                    52.dp
                ),

            shape =
                RoundedCornerShape(
                    17.dp
                ),

            border =
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,

                    color =
                        accentColor.copy(
                            alpha = 0.70f
                        )
                ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            BTN_BG,

                        contentColor =
                            ComposeColor.White
                    )
        ) {

            Text(
                text =
                    text,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }

    /*
     * ========================================================
     * SMALL BUTTON
     * ========================================================
     */
    @Composable
    private fun GlassSmallButton(
        modifier: Modifier = Modifier,
        text: String,
        accent: Accent,
        onClick: () -> Unit
    ) {

        val accentColor =
            when (accent) {

                Accent.GREEN ->
                    ACCENT_GREEN

                Accent.GOLD ->
                    ACCENT_GOLD

                Accent.BLUE ->
                    ACCENT_BLUE
            }

        OutlinedButton(
            onClick =
                onClick,

            modifier =
                modifier.size(
                    46.dp
                ),

            shape =
                RoundedCornerShape(
                    14.dp
                ),

            border =
                androidx.compose.foundation.BorderStroke(
                    1.dp,

                    accentColor.copy(
                        alpha = 0.70f
                    )
                ),

            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(
                        0.dp
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            BTN_BG,

                        contentColor =
                            ComposeColor.White
                    )
        ) {

            Text(
                text =
                    text,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }

    /*
     * ========================================================
     * NOTIFICATION
     * ========================================================
     */
    @Composable
    private fun JoinNotification(
        text: String
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.BottomCenter
        ) {

            Text(
                text =
                    text,

                modifier =
                    Modifier.padding(
                        bottom = 105.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    ComposeColor.White
            )
        }
    }

    /*
     * ========================================================
     * WELCOME OVERLAY
     * ========================================================
     *
     * بهینه‌سازی‌ها:
     *
     * - ارتفاع کادر اسکرول از 280dp به 210dp کاهش داده شد
     *   تا layout pass سبک‌تر باشد.
     *
     * - درون کادر اسکرول از Arrangement.spacedBy استفاده
     *   می‌شود تا layout node کمتری ساخته شود.
     *
     * - رنگ‌ها همه از ثابت‌های object می‌آیند.
     * ========================================================
     */
    @Composable
    private fun WelcomeOverlay() {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        SCRIM
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp
                        )
                        .background(
                            PANEL_BG,
                            RoundedCornerShape(
                                24.dp
                            )
                        )
                        .border(
                            width = 1.dp,
                            color =
                                PANEL_BORDER,
                            shape =
                                RoundedCornerShape(
                                    24.dp
                                )
                        )
                        .padding(
                            18.dp
                        ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "تهران بزرگ",
                    fontSize =
                        26.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        ACCENT_GREEN
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        "پیام مهم سازنده",
                    fontSize =
                        14.sp,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        TEXT_SOFT
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                val scrollState =
                    rememberScrollState()

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                210.dp
                            )
                            .background(
                                INFO_BG,
                                RoundedCornerShape(
                                    16.dp
                                )
                            )
                            .border(
                                width = 1.dp,
                                color =
                                    INFO_BORDER,
                                shape =
                                    RoundedCornerShape(
                                        16.dp
                                    )
                            )
                            .verticalScroll(
                                scrollState
                            )
                            .padding(
                                14.dp
                            ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "سلام! به تهران بزرگ خوش آمدید\n\n" +
                            "این بازی توسط XXX SONIC ادیت شده است " +
                            "و در مراحلی نیست که انتشار پیدا کند...\n\n" +
                            "شما اول تستر هستید!\n\n" +
                            "نباید این نسخه را در اختیار کسی بگذارید " +
                            "و نسخه هنوز کامل نشده است.\n\n" +
                            "سازنده اصلی این بازی :\n" +
                            "محمد علیزاده | Mohammad Alizadeh\n\n" +
                            "برای ارتباط با سازنده از لینک‌های زیر استفاده کنید 👇",
                        modifier =
                            Modifier.fillMaxWidth(),
                        fontSize =
                            15.sp,
                        lineHeight =
                            23.sp,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            TEXT_BRIGHT
                    )

                    WelcomeLinkButton(
                        text =
                            "🌐  لینک سایت اصلی بازی",
                        accent =
                            Accent.GREEN,
                        onClick = {
                            openExternalLink(
                                WELCOME_SITE_URL
                            )
                        }
                    )

                    WelcomeLinkButton(
                        text =
                            "▶  کانال یوتیوب سازنده",
                        accent =
                            Accent.BLUE,
                        onClick = {
                            openExternalLink(
                                WELCOME_YOUTUBE_URL
                            )
                        }
                    )

                    WelcomeLinkButton(
                        text =
                            "💬  دیسکورد سازنده بازی",
                        accent =
                            Accent.GOLD,
                        onClick = {
                            openExternalLink(
                                WELCOME_DISCORD_URL
                            )
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                /*
                 * =============================================
                 * CONFIRM BUTTON
                 * =============================================
                 */
                OutlinedButton(
                    onClick = {
                        confirmWelcomeMessage()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                54.dp
                            ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        ),
                    border =
                        androidx.compose
                            .foundation
                            .BorderStroke(
                                width = 1.dp,
                                color =
                                    CONFIRM_BORDER
                            ),
                    colors =
                        ButtonDefaults
                            .outlinedButtonColors(
                                backgroundColor =
                                    CONFIRM_BG,
                                contentColor =
                                    ComposeColor.White
                            )
                ) {

                    Text(
                        text =
                            "باشه، متوجه شدم",
                        fontSize =
                            15.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }

    /*
     * ========================================================
     * WELCOME LINK BUTTON
     * ========================================================
     */
    @Composable
    private fun WelcomeLinkButton(
        text: String,
        accent: Accent,
        onClick: () -> Unit
    ) {

        val accentColor =
            when (accent) {

                Accent.GREEN ->
                    ACCENT_GREEN

                Accent.GOLD ->
                    ACCENT_GOLD

                Accent.BLUE ->
                    ACCENT_BLUE
            }

        OutlinedButton(
            onClick =
                onClick,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        46.dp
                    ),

            shape =
                RoundedCornerShape(
                    14.dp
                ),

            border =
                androidx.compose
                    .foundation
                    .BorderStroke(
                        width = 1.dp,
                        color =
                            accentColor.copy(
                                alpha = 0.65f
                            )
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            BTN_BG_LIGHT,
                        contentColor =
                            ComposeColor.White
                    )
        ) {

            Text(
                text =
                    text,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }

    /*
     * ========================================================
     * IRAN TIME
     * ========================================================
     */
    private fun getIranTime():
        String {

        return try {

            val formatter =
                SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.US
                )

            formatter.timeZone =
                TimeZone.getTimeZone(
                    "Asia/Tehran"
                )

            formatter.format(
                Date()
            )

        } catch (_: Throwable) {

            "--:--:--"
        }
    }

    /*
     * ========================================================
     * LIFECYCLE OWNER
     * ========================================================
     */
    private class GameLifecycleOwner :
        LifecycleOwner,
        SavedStateRegistryOwner {

        private val lifecycleRegistry =
            LifecycleRegistry(this)

        private val savedStateController =
            SavedStateRegistryController
                .create(this)

        override val lifecycle:
            Lifecycle
            get() =
                lifecycleRegistry

        override val savedStateRegistry =
            savedStateController
                .savedStateRegistry

        fun attachAndRestore() {

            savedStateController
                .performAttach()

            savedStateController
                .performRestore(null)

            lifecycleRegistry
                .handleLifecycleEvent(
                    Lifecycle.Event.ON_CREATE
                )

            lifecycleRegistry
                .handleLifecycleEvent(
                    Lifecycle.Event.ON_START
                )

            lifecycleRegistry
                .handleLifecycleEvent(
                    Lifecycle.Event.ON_RESUME
                )
        }

        fun destroy() {

            try {

                lifecycleRegistry
                    .handleLifecycleEvent(
                        Lifecycle.Event.ON_PAUSE
                    )

                lifecycleRegistry
                    .handleLifecycleEvent(
                        Lifecycle.Event.ON_STOP
                    )

                lifecycleRegistry
                    .handleLifecycleEvent(
                        Lifecycle.Event.ON_DESTROY
                    )

            } catch (_: Throwable) {
            }
        }
    }
}