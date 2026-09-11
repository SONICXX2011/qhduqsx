package com.example.gameui

import android.app.Activity
import android.graphics.Color
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner

import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.ViewTreeSavedStateRegistryOwner


object GameMenu {

    private const val TAG = "GameMenu"

    private val mainHandler =
        Handler(Looper.getMainLooper())

    @Volatile
    private var currentGameMenu: Int = -1

    @Volatile
    private var networkActive: Boolean = false

    private var composeView: ComposeView? = null

    private var lifecycleOwner: GameLifecycleOwner? = null

    private var visible by mutableStateOf(false)

    private var currentPage by mutableStateOf(Page.MAIN)

    private var notificationText by mutableStateOf<String?>(null)

    private var notificationToken: Long = 0L

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

    // =========================================================
    // SHOW
    // =========================================================

    @JvmStatic
    fun show(activity: Activity) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                show(activity)
            }
            return
        }

        try {

            val decorView =
                activity.window?.decorView as? ViewGroup

            if (decorView == null) {
                Log.e(TAG, "show(): decorView == null")
                return
            }

            val existingView =
                composeView

            if (existingView != null) {

                val owner =
                    lifecycleOwner

                if (owner != null) {

                    ViewTreeLifecycleOwner.set(
                        decorView,
                        owner
                    )

                    ViewTreeSavedStateRegistryOwner.set(
                        decorView,
                        owner
                    )

                    ViewTreeLifecycleOwner.set(
                        existingView,
                        owner
                    )

                    ViewTreeSavedStateRegistryOwner.set(
                        existingView,
                        owner
                    )
                }

                if (existingView.parent == null) {
                    decorView.addView(existingView)
                }

                updateVisibility()

                Log.d(
                    TAG,
                    "show(): existing ComposeView reused"
                )

                return
            }

            /*
             * Lifecycle + SavedState owner
             */
            val owner =
                GameLifecycleOwner()

            owner.attachAndRestore()

            lifecycleOwner =
                owner

            /*
             * IMPORTANT:
             *
             * Both owners are installed BEFORE
             * ComposeView is attached.
             */
            ViewTreeLifecycleOwner.set(
                decorView,
                owner
            )

            ViewTreeSavedStateRegistryOwner.set(
                decorView,
                owner
            )

            /*
             * Create ComposeView.
             */
            val view =
                ComposeView(activity)

            /*
             * Explicitly install both owners on ComposeView.
             */
            ViewTreeLifecycleOwner.set(
                view,
                owner
            )

            ViewTreeSavedStateRegistryOwner.set(
                view,
                owner
            )

            /*
             * Transparent background.
             *
             * This prevents the custom UI from adding
             * a dark full-screen background.
             */
            view.setBackgroundColor(
                Color.TRANSPARENT
            )

            view.alpha =
                1f

            view.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

            /*
             * Hidden until Frida says:
             * setGameState(0, false)
             */
            view.visibility =
                View.GONE

            /*
             * Compose content.
             */
            view.setContent {
                GameMenuRoot()
            }

            composeView =
                view

            /*
             * Attach after ViewTree owners exist.
             */
            decorView.addView(
                view
            )

            updateVisibility()

            Log.d(
                TAG,
                "show(): ComposeView created and attached"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "show(): failed",
                t
            )

            composeView =
                null

            try {
                lifecycleOwner?.destroy()
            } catch (_: Throwable) {
            }

            lifecycleOwner =
                null
        }
    }

    // =========================================================
    // HIDE
    // =========================================================

    @JvmStatic
    fun hide() {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                hide()
            }
            return
        }

        visible =
            false

        composeView?.visibility =
            View.GONE

        Log.d(
            TAG,
            "hide()"
        )
    }

    // =========================================================
    // FRIDA STATE
    // =========================================================

    @JvmStatic
    fun setGameState(
        menu: Int,
        active: Boolean
    ) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                setGameState(
                    menu,
                    active
                )
            }
            return
        }

        currentGameMenu =
            menu

        networkActive =
            active

        when {

            active -> {
                visible =
                    false
            }

            menu == 0 -> {
                currentPage =
                    Page.MAIN

                visible =
                    true
            }

            menu == 3 -> {
                currentPage =
                    Page.CHARACTER

                visible =
                    true
            }

            else -> {
                visible =
                    false
            }
        }

        updateVisibility()

        Log.d(
            TAG,
            "STATE menu=$menu active=$active page=$currentPage visible=$visible"
        )
    }

    // =========================================================
    // START GAME
    // =========================================================

    @JvmStatic
    fun onStartGameClicked() {

        Log.d(
            TAG,
            "START_GAME_CLICKED"
        )

        showJoinNotification()
    }

    // =========================================================
    // NOTIFICATION
    // =========================================================

    @JvmStatic
    fun showJoinNotification() {

        if (Looper.myLooper() != Looper.getMainLooper()) {
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

        mainHandler.postDelayed(
            {
                if (notificationToken == token) {
                    notificationText = null
                }
            },
            3000L
        )
    }

    // =========================================================
    // CHARACTER
    // =========================================================

    @JvmStatic
    fun openCharacterFromBridge() {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post {
                openCharacterFromBridge()
            }
            return
        }

        currentPage =
            Page.CHARACTER

        if (!networkActive) {
            visible =
                true
        }

        updateVisibility()

        Log.d(
            TAG,
            "OPEN_CHARACTER_FROM_BRIDGE"
        )
    }

    @JvmStatic
    fun openCharacter() {
        openCharacterFromBridge()
    }

    // =========================================================
    // VISIBILITY
    // =========================================================

    private fun updateVisibility() {

        val view =
            composeView
                ?: return

        val shouldShow =
            visible &&
                !networkActive

        view.visibility =
            if (shouldShow) {
                View.VISIBLE
            } else {
                View.GONE
            }

        view.alpha =
            1f
    }

    // =========================================================
    // ROOT
    // =========================================================

    @Composable
    private fun GameMenuRoot() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            if (
                visible &&
                !networkActive
            ) {

                when (currentPage) {

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
    }

    // =========================================================
    // MAIN PAGE
    // =========================================================

    @Composable
    private fun MainPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

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

                /*
                 * Character
                 */
                GlassButton(
                    modifier =
                        Modifier.width(118.dp),

                    text =
                        "Character",

                    accent =
                        Accent.GOLD,

                    onClick = {

                        currentPage =
                            Page.CHARACTER

                        /*
                         * Signal Frida to press the
                         * original Unity Character button.
                         */
                        UnityGameUIBridge
                            .requestUnityCharacter()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                /*
                 * Start Game
                 */
                GlassButton(
                    modifier =
                        Modifier.width(138.dp),

                    text =
                        "Start Game",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        /*
                         * Signal Frida.
                         * Frida performs actual connection.
                         */
                        UnityGameUIBridge
                            .requestStartGame()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                /*
                 * Help
                 */
                GlassButton(
                    modifier =
                        Modifier.width(105.dp),

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

    // =========================================================
    // CHARACTER PAGE
    // =========================================================

    @Composable
    private fun CharacterPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            GlassHeader(
                title =
                    "Character",

                onBack = {
                    currentPage =
                        Page.MAIN
                }
            )

            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 22.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                shape =
                    RoundedCornerShape(24.dp),

                color =
                    ComposeColor(
                        0xCC101816
                    )
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "CHARACTER",

                        fontSize =
                            22.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ComposeColor.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(120.dp)
                                .background(
                                    brush =
                                        Brush.linearGradient(
                                            listOf(
                                                ComposeColor(
                                                    0xFF173529
                                                ),
                                                ComposeColor(
                                                    0xFF0D1713
                                                )
                                            )
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            28.dp
                                        )
                                )
                                .border(
                                    width =
                                        1.dp,

                                    color =
                                        ComposeColor(
                                            0x5548E39A
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            28.dp
                                        )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "C",

                            fontSize =
                                54.sp,

                            fontWeight =
                                FontWeight.Black,

                            color =
                                ComposeColor(
                                    0xFF62E6A4
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
                    )

                    InfoRow(
                        title =
                            "Role",

                        value =
                            "Null"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    InfoRow(
                        title =
                            "Money",

                        value =
                            "Null"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
                    )

                    GlassButton(
                        modifier =
                            Modifier.fillMaxWidth(),

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
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    @Composable
    private fun LoginPage() {

        var password by remember {
            mutableStateOf("")
        }

        var confirmPassword by remember {
            mutableStateOf("")
        }

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            GlassHeader(
                title =
                    "Login / Register",

                onBack = {
                    currentPage =
                        Page.CHARACTER
                }
            )

            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 22.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                shape =
                    RoundedCornerShape(24.dp),

                color =
                    ComposeColor(
                        0xCC101816
                    )
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(22.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "ACCOUNT",

                        fontSize =
                            21.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ComposeColor.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    OutlinedTextField(
                        value =
                            password,

                        onValueChange = {
                            password =
                                it
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
                            Modifier.height(12.dp)
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
                            Modifier.height(18.dp)
                    )

                    InfoRow(
                        title =
                            "Role",

                        value =
                            "Null"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    InfoRow(
                        title =
                            "Money",

                        value =
                            "Null"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
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
    }

    // =========================================================
    // HELP PAGE
    // =========================================================

    @Composable
    private fun HelpPage() {

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            GlassHeader(
                title =
                    "Help",

                onBack = {
                    currentPage =
                        Page.MAIN
                }
            )

            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 22.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                shape =
                    RoundedCornerShape(24.dp),

                color =
                    ComposeColor(
                        0xCC101816
                    )
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(30.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "?",

                        fontSize =
                            50.sp,

                        fontWeight =
                            FontWeight.Black,

                        color =
                            ComposeColor(
                                0xFF62E6A4
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "به زودی",

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ComposeColor.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "Help system will be available soon.",

                        fontSize =
                            14.sp,

                        color =
                            ComposeColor(
                                0xFFB7C5BE
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
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
    }

    // =========================================================
    // HEADER
    // =========================================================

    @Composable
    private fun GlassHeader(
        title: String,
        onBack: () -> Unit
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 30.dp
                    )
        ) {

            GlassSmallButton(
                modifier =
                    Modifier.align(
                        Alignment.CenterStart
                    ),

                text =
                    "<",

                accent =
                    Accent.GREEN,

                onClick =
                    onBack
            )

            Text(
                text =
                    title,

                modifier =
                    Modifier.align(
                        Alignment.Center
                    ),

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    ComposeColor.White
            )
        }
    }

    // =========================================================
    // INFO ROW
    // =========================================================

    @Composable
    private fun InfoRow(
        title: String,
        value: String
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        ComposeColor(
                            0x331C2923
                        ),

                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
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
                    14.sp,

                color =
                    ComposeColor(
                        0xFF9BB0A7
                    )
            )

            Text(
                text =
                    value,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    ComposeColor.White
            )
        }
    }

    // =========================================================
    // GLASS BUTTON
    // =========================================================

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
                    ComposeColor(
                        0xFF58E59A
                    )

                Accent.GOLD ->
                    ComposeColor(
                        0xFFF0C85C
                    )

                Accent.BLUE ->
                    ComposeColor(
                        0xFF72B8FF
                    )
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
                androidx.compose.foundation
                    .BorderStroke(
                        width =
                            1.dp,

                        color =
                            accentColor.copy(
                                alpha =
                                    0.65f
                            )
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            ComposeColor(
                                0xAA111A16
                            ),

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

    // =========================================================
    // SMALL BUTTON
    // =========================================================

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
                    ComposeColor(
                        0xFF58E59A
                    )

                Accent.GOLD ->
                    ComposeColor(
                        0xFFF0C85C
                    )

                Accent.BLUE ->
                    ComposeColor(
                        0xFF72B8FF
                    )
            }

        OutlinedButton(
            onClick =
                onClick,

            modifier =
                modifier.size(
                    48.dp
                ),

            shape =
                RoundedCornerShape(
                    15.dp
                ),

            border =
                androidx.compose.foundation
                    .BorderStroke(
                        width =
                            1.dp,

                        color =
                            accentColor.copy(
                                alpha =
                                    0.65f
                            )
                    ),

            contentPadding =
                androidx.compose.foundation
                    .layout
                    .PaddingValues(
                        0.dp
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            ComposeColor(
                                0xAA111A16
                            ),

                        contentColor =
                            ComposeColor.White
                    )
        ) {

            Text(
                text =
                    text,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }

    // =========================================================
    // JOIN NOTIFICATION
    // =========================================================

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

            Surface(
                modifier =
                    Modifier
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 108.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                shape =
                    RoundedCornerShape(
                        18.dp
                    ),

                color =
                    ComposeColor(
                        0xE619211D
                    )
            ) {

                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 15.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier =
                            Modifier
                                .size(
                                    10.dp
                                )
                                .background(
                                    ComposeColor(
                                        0xFF58E59A
                                    ),

                                    RoundedCornerShape(
                                        50
                                    )
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                12.dp
                            )
                    )

                    Text(
                        text =
                            text,

                        fontSize =
                            14.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            ComposeColor.White
                    )
                }
            }
        }
    }

    // =========================================================
    // LIFECYCLE + SAVED STATE
    // =========================================================

    private class GameLifecycleOwner :
        LifecycleOwner,
        SavedStateRegistryOwner {

        private val lifecycleRegistry =
            LifecycleRegistry(this)

        private val savedStateController =
            SavedStateRegistryController.create(
                this
            )

        override val lifecycle: Lifecycle
            get() =
                lifecycleRegistry

        override val savedStateRegistry =
            savedStateController.savedStateRegistry

        fun attachAndRestore() {

            /*
             * Correct order:
             *
             * 1. Attach
             * 2. Restore
             * 3. CREATE
             * 4. START
             * 5. RESUME
             */
            savedStateController.performAttach()

            savedStateController.performRestore(
                null
            )

            lifecycleRegistry.handleLifecycleEvent(
                Lifecycle.Event.ON_CREATE
            )

            lifecycleRegistry.handleLifecycleEvent(
                Lifecycle.Event.ON_START
            )

            lifecycleRegistry.handleLifecycleEvent(
                Lifecycle.Event.ON_RESUME
            )
        }

        fun destroy() {

            try {

                lifecycleRegistry.handleLifecycleEvent(
                    Lifecycle.Event.ON_PAUSE
                )

                lifecycleRegistry.handleLifecycleEvent(
                    Lifecycle.Event.ON_STOP
                )

                lifecycleRegistry.handleLifecycleEvent(
                    Lifecycle.Event.ON_DESTROY
                )

            } catch (_: Throwable) {
            }
        }
    }
}