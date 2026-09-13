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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
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

    private val mainHandler =
        Handler(Looper.getMainLooper())

    // =========================================================
    // UNITY / GAME STATE
    // =========================================================

    @Volatile
    private var currentGameMenu = -1

    @Volatile
    private var networkActive = false

    // =========================================================
    // CHARACTER DATA
    // =========================================================

    @Volatile
    private var characterName = "Null"

    @Volatile
    private var characterRole = "Null"

    @Volatile
    private var characterMoney = "Null"

    // =========================================================
    // ANDROID UI
    // =========================================================

    private var composeView: ComposeView? = null

    private var lifecycleOwner: GameLifecycleOwner? = null

    private var visible by mutableStateOf(false)

    private var currentPage by mutableStateOf(Page.MAIN)

    private var notificationText by mutableStateOf<String?>(null)

    private var notificationToken = 0L

    // =========================================================
    // PAGE
    // =========================================================

    private enum class Page {
        MAIN,
        CHARACTER,
        LOGIN,
        HELP
    }

    // =========================================================
    // ACCENT
    // =========================================================

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
                activity.window?.decorView as? ViewGroup

            if (decorView == null) {

                Log.e(
                    TAG,
                    "show(): decorView == null"
                )

                return
            }

            /*
             * اگر ComposeView قبلاً ساخته شده،
             * همان View را دوباره استفاده می‌کنیم.
             */
            val existing =
                composeView

            if (existing != null) {

                val owner =
                    lifecycleOwner

                if (owner != null) {

                    decorView.setViewTreeLifecycleOwner(
                        owner
                    )

                    decorView.setViewTreeSavedStateRegistryOwner(
                        owner
                    )

                    existing.setViewTreeLifecycleOwner(
                        owner
                    )

                    existing.setViewTreeSavedStateRegistryOwner(
                        owner
                    )
                }

                if (existing.parent == null) {

                    decorView.addView(
                        existing
                    )
                }

                /*
                 * خیلی مهم:
                 *
                 * پس‌زمینه خود ComposeView کاملاً شفاف است.
                 * هیچ تاریک‌سازی روی Game نمی‌شود.
                 */
                existing.setBackgroundColor(
                    Color.TRANSPARENT
                )

                updateVisibility()

                Log.d(
                    TAG,
                    "show(): existing ComposeView reused"
                )

                return
            }

            /*
             * Owner مشترک برای:
             *
             * LifecycleOwner
             * SavedStateRegistryOwner
             */
            val owner =
                GameLifecycleOwner()

            owner.attachAndRestore()

            lifecycleOwner =
                owner

            /*
             * دقیقاً قبل از attach شدن ComposeView
             * هر دو Owner نصب می‌شوند.
             */
            decorView.setViewTreeLifecycleOwner(
                owner
            )

            decorView.setViewTreeSavedStateRegistryOwner(
                owner
            )

            /*
             * ComposeView
             */
            val view =
                ComposeView(activity)

            view.setViewTreeLifecycleOwner(
                owner
            )

            view.setViewTreeSavedStateRegistryOwner(
                owner
            )

            view.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

            /*
             * کاملاً Transparent.
             *
             * هیچ background تیره‌ای روی Unity نمی‌افتد.
             */
            view.setBackgroundColor(
                Color.TRANSPARENT
            )

            /*
             * ابتدا مخفی.
             */
            view.visibility =
                View.GONE

            /*
             * Compose content
             */
            view.setContent {
                GameMenuRoot()
            }

            composeView =
                view

            /*
             * بعد از ثبت Ownerها attach می‌کنیم.
             */
            decorView.addView(
                view
            )

            updateVisibility()

            Log.d(
                TAG,
                "show(): ComposeView created successfully"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "show(): failed",
                t
            )

            composeView = null

            try {

                lifecycleOwner?.destroy()

            } catch (_: Throwable) {
            }

            lifecycleOwner = null
        }
    }

    // =========================================================
    // HIDE
    // =========================================================

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

            visible = false

            composeView?.visibility =
                View.GONE

            Log.d(
                TAG,
                "hide()"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "hide(): failed",
                t
            )
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @JvmStatic
    fun destroy() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                destroy()
            }

            return
        }

        try {

            visible = false

            composeView?.visibility =
                View.GONE

            val view =
                composeView

            if (view != null) {

                val parent =
                    view.parent

                if (parent is ViewGroup) {

                    parent.removeView(
                        view
                    )
                }
            }

            composeView = null

            try {
                lifecycleOwner?.destroy()
            } catch (_: Throwable) {
            }

            lifecycleOwner = null

            notificationText = null

            currentPage =
                Page.MAIN

            characterName =
                "Null"

            characterRole =
                "Null"

            characterMoney =
                "Null"

            Log.d(
                TAG,
                "destroy()"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "destroy(): failed",
                t
            )
        }
    }

    // =========================================================
    // GAME STATE
    // =========================================================

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

            /*
             * ==================================================
             * NETWORK ACTIVE
             * ==================================================
             *
             * وقتی بازی آنلاین شد،
             * UI کامل مخفی می‌شود.
             */
            if (active) {

                visible = false

                updateVisibility()

                Log.d(
                    TAG,
                    "STATE network active -> HIDE"
                )

                return
            }

            /*
             * ==================================================
             * MAIN
             * ==================================================
             *
             * 0 = MAIN
             */
            if (menu == 0) {

                currentPage =
                    Page.MAIN

                visible =
                    true

                updateVisibility()

                Log.d(
                    TAG,
                    "STATE MAIN -> SHOW"
                )

                return
            }

            /*
             * ==================================================
             * CHARACTER
             * ==================================================
             *
             * 3 = CHARACTER
             *
             * تا وقتی State روی 3 است:
             * UI Character باقی می‌ماند.
             *
             * هیچ Timeout ندارد.
             */
            if (menu == 3) {

                currentPage =
                    Page.CHARACTER

                visible =
                    true

                updateVisibility()

                Log.d(
                    TAG,
                    "STATE CHARACTER -> SHOW"
                )

                return
            }

            /*
             * ==================================================
             * OTHER MENUS
             * ==================================================
             *
             * LAN
             * COMMUNITY
             * SETTINGS
             * ABOUT
             *
             * UI سفارشی مخفی.
             */
            visible =
                false

            updateVisibility()

            Log.d(
                TAG,
                "STATE menu=$menu -> HIDE"
            )

        } catch (t: Throwable) {

            Log.e(
                TAG,
                "setGameState(): failed",
                t
            )
        }
    }

    // =========================================================
    // CHARACTER DATA
    // =========================================================

    @JvmStatic
    fun setCharacterInfo(
        name: String?,
        role: String?,
        money: String?
    ) {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {

                setCharacterInfo(
                    name,
                    role,
                    money
                )
            }

            return
        }

        characterName =
            if (
                name.isNullOrBlank()
            ) {
                "Null"
            } else {
                name
            }

        characterRole =
            if (
                role.isNullOrBlank()
            ) {
                "Null"
            } else {
                role
            }

        characterMoney =
            if (
                money.isNullOrBlank()
            ) {
                "Null"
            } else {
                money
            }

        Log.d(
            TAG,
            "CHARACTER_INFO name=$characterName role=$characterRole money=$characterMoney"
        )
    }

    @JvmStatic
    fun clearCharacterInfo() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                clearCharacterInfo()
            }

            return
        }

        characterName =
            "Null"

        characterRole =
            "Null"

        characterMoney =
            "Null"
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
    // LOGIN
    // =========================================================

    @JvmStatic
    fun onLoginClicked() {

        Log.d(
            TAG,
            "LOGIN_CLICKED"
        )

        /*
         * فعلاً فقط Event ثبت می‌شود.
         *
         * منطق Login بعداً می‌تواند توسط Frida
         * یا Bridge پردازش شود.
         */
        UnityGameUIBridge.requestLogin()
    }

    // =========================================================
    // NOTIFICATION
    // =========================================================

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

        mainHandler.postDelayed(
            {

                if (
                    notificationToken ==
                    token
                ) {

                    notificationText =
                        null
                }

            },
            3000L
        )
    }

    @JvmStatic
    fun clearJoinNotification() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                clearJoinNotification()
            }

            return
        }

        notificationToken++

        notificationText =
            null
    }

    // =========================================================
    // CHARACTER BRIDGE
    // =========================================================

    @JvmStatic
    fun openCharacterFromBridge() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                openCharacterFromBridge()
            }

            return
        }

        /*
         * فقط در صورتی که Network فعال نباشد.
         */
        if (!networkActive) {

            currentPage =
                Page.CHARACTER

            visible =
                true

            updateVisibility()
        }

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
    // HELP
    // =========================================================

    @JvmStatic
    fun openHelp() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                openHelp()
            }

            return
        }

        if (networkActive) {
            return
        }

        currentPage =
            Page.HELP

        visible =
            true

        updateVisibility()
    }

    @JvmStatic
    fun closeHelp() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                closeHelp()
            }

            return
        }

        currentPage =
            Page.MAIN

        visible =
            !networkActive

        updateVisibility()
    }

    // =========================================================
    // BACK FROM CHARACTER
    // =========================================================

    @JvmStatic
    fun closeCharacterToMain() {

        if (
            Looper.myLooper() !=
            Looper.getMainLooper()
        ) {

            mainHandler.post {
                closeCharacterToMain()
            }

            return
        }

        /*
         * این فقط Navigation داخلی Kotlin است.
         *
         * BackMenu واقعی Unity باید State را به 0
         * برگرداند تا setGameState() هم Main را نمایش دهد.
         */
        currentPage =
            Page.MAIN

        if (!networkActive) {
            visible =
                true
        }

        updateVisibility()
    }

    // =========================================================
    // VISIBILITY
    // =========================================================

    private fun updateVisibility() {

        val view =
            composeView

        if (view == null) {
            return
        }

        val shouldShow =
            visible &&
            !networkActive

        view.visibility =
            if (shouldShow) {
                View.VISIBLE
            } else {
                View.GONE
            }

        /*
         * Alpha همیشه 1.
         *
         * هیچ fade تاریک یا نیمه‌شفاف روی کل صفحه نداریم.
         */
        view.alpha =
            1f

        view.setBackgroundColor(
            Color.TRANSPARENT
        )
    }

    // =========================================================
    // ROOT
    // =========================================================

    @Composable
    private fun GameMenuRoot() {

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        ComposeColor.Transparent
                    )
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

            notificationText?.let {
                text ->

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
                Modifier
                    .fillMaxSize()
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

                GlassButton(
                    modifier =
                        Modifier.width(118.dp),

                    text =
                        "Character",

                    accent =
                        Accent.GOLD,

                    onClick = {

                        /*
                         * UI را باز می‌کنیم.
                         *
                         * Unity state نیز از سمت Frida
                         * به 3 تغییر خواهد کرد.
                         */
                        openCharacterFromBridge()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                GlassButton(
                    modifier =
                        Modifier.width(138.dp),

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
                        Modifier.width(12.dp)
                )

                GlassButton(
                    modifier =
                        Modifier.width(105.dp),

                    text =
                        "?  Help",

                    accent =
                        Accent.BLUE,

                    onClick = {

                        openHelp()
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

        /*
         * ساعت ایران.
         *
         * این State هر ثانیه update می‌شود.
         */
        var iranTime by remember {
            mutableStateOf(
                getIranTime()
            )
        }

        LaunchedEffect(
            currentPage
        ) {

            while (
                currentPage ==
                Page.CHARACTER
            ) {

                iranTime =
                    getIranTime()

                kotlinx.coroutines.delay(
                    1000L
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
        ) {

            /*
             * هیچ عنوان Character،
             * هیچ آیکون،
             * هیچ عکس،
             * هیچ C،
             * هیچ پنل بزرگی وسط صفحه.
             *
             * فقط متن اطلاعات.
             */

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(
                            start = 22.dp,
                            end = 22.dp,
                            bottom = 38.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                CharacterInfoText(
                    label =
                        "Name",

                    value =
                        characterName
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                CharacterInfoText(
                    label =
                        "Role",

                    value =
                        characterRole
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                CharacterInfoText(
                    label =
                        "Money",

                    value =
                        characterMoney
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                CharacterInfoText(
                    label =
                        "Time",

                    value =
                        iranTime
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                GlassButton(
                    modifier =
                        Modifier.width(160.dp),

                    text =
                        "Login",

                    accent =
                        Accent.GREEN,

                    onClick = {

                        onLoginClicked()
                    }
                )
            }
        }
    }

    // =========================================================
    // CHARACTER TEXT
    // =========================================================

    @Composable
    private fun CharacterInfoText(
        label: String,
        value: String
    ) {

        Text(
            text =
                "$label: $value",

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 4.dp,
                        vertical = 1.dp
                    ),

            color =
                ComposeColor.White,

            fontSize =
                15.sp,

            fontWeight =
                FontWeight.Medium,

            textAlign =
                TextAlign.Center
        )
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
                    RoundedCornerShape(
                        22.dp
                    ),

                /*
                 * فقط خود پنل کمی زمینه دارد.
                 *
                 * کل صفحه شفاف است.
                 */
                color =
                    ComposeColor(
                        0xE6141D18
                    )
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                22.dp
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "Login",

                        fontSize =
                            20.sp,

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
                            password = it
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth(),

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
                            Modifier
                                .fillMaxWidth(),

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

                    GlassButton(
                        modifier =
                            Modifier.fillMaxWidth(),

                        text =
                            "Continue",

                        accent =
                            Accent.GREEN,

                        onClick = {

                            onLoginClicked()
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    GlassSmallButton(
                        modifier =
                            Modifier.fillMaxWidth(),

                        text =
                            "Back",

                        accent =
                            Accent.BLUE,

                        onClick = {

                            currentPage =
                                Page.CHARACTER
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

            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .padding(
                            horizontal = 24.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight(),

                shape =
                    RoundedCornerShape(
                        22.dp
                    ),

                color =
                    ComposeColor(
                        0xEA131C18
                    )
            ) {

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                20.dp
                            )
                ) {

                    /*
                     * Header فقط برای Help.
                     *
                     * X اینجا واقعاً Help را می‌بندد.
                     */
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                "Help",

                            fontSize =
                                19.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                ComposeColor.White
                        )

                        GlassSmallButton(
                            modifier =
                                Modifier.width(
                                    48.dp
                                ),

                            text =
                                "X",

                            accent =
                                Accent.BLUE,

                            onClick = {

                                closeHelp()
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Text(
                        text =
                            "Character برای نمایش اطلاعات کاراکتر بازی است.",

                        modifier =
                            Modifier.fillMaxWidth(),

                        fontSize =
                            13.sp,

                        color =
                            ComposeColor.White,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Start Game برای شروع فرآیند اتصال است.",

                        modifier =
                            Modifier.fillMaxWidth(),

                        fontSize =
                            13.sp,

                        color =
                            ComposeColor.White,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Login برای ورود به حساب کاربری است.",

                        modifier =
                            Modifier.fillMaxWidth(),

                        fontSize =
                            13.sp,

                        color =
                            ComposeColor.White,

                        textAlign =
                            TextAlign.Center
                    )
                }
            }
        }
    }

    // =========================================================
    // BUTTON
    // =========================================================

    @Composable
    private fun GlassButton(
        modifier: Modifier =
            Modifier,

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
                modifier
                    .height(
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
                                    0.70f
                            )
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            ComposeColor(
                                0xB3111714
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
        modifier: Modifier =
            Modifier,

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
                    42.dp
                ),

            shape =
                RoundedCornerShape(
                    13.dp
                ),

            border =
                androidx.compose.foundation
                    .BorderStroke(
                        width =
                            1.dp,

                        color =
                            accentColor.copy(
                                alpha =
                                    0.70f
                            )
                    ),

            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        backgroundColor =
                            ComposeColor(
                                0xB3111714
                            ),

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

    // =========================================================
    // NOTIFICATION
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
                        17.dp
                    ),

                color =
                    ComposeColor(
                        0xE619211D
                    )
            ) {

                Text(
                    text =
                        text,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 18.dp,
                                vertical = 14.dp
                            ),

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ComposeColor.White,

                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }

    // =========================================================
    // IRAN TIME
    // =========================================================

    private fun getIranTime(): String {

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

    // =========================================================
    // LIFECYCLE / SAVED STATE
    // =========================================================

    private class GameLifecycleOwner :
        LifecycleOwner,
        SavedStateRegistryOwner {

        private val lifecycleRegistry =
            LifecycleRegistry(
                this
            )

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
             * ترتیب صحیح همان نسخه سالم پروژه.
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