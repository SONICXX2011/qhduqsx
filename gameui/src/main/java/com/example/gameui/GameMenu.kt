package com.example.gameui

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

object GameMenu {

    private const val TAG = "GameMenu"

    /*
     * این مقادیر فقط state مربوط به نمایش UI هستند.
     * هیچ منطق شبکه‌ای داخل این فایل وجود ندارد.
     */
    private const val MAIN_MENU = 0
    private const val CHARACTER_MENU = 3

    @Volatile
    private var composeView: ComposeView? = null

    @Volatile
    private var lifecycleOwner: GameLifecycleOwner? = null

    @Volatile
    private var activity: Activity? = null

    @Volatile
    private var currentMenu: Int = -1

    @Volatile
    private var networkActive: Boolean = false

    var helpOpen by mutableStateOf(false)
        private set

    var loginOpen by mutableStateOf(false)
        private set

    var joinNotification by mutableStateOf(false)
        private set

    @Volatile
    private var passwordText: String = ""

    @Volatile
    private var confirmPasswordText: String = ""

    // ---------------------------------------------------------
    // SHOW
    // ---------------------------------------------------------

    @JvmStatic
    fun show(host: Activity) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            host.runOnUiThread {
                show(host)
            }
            return
        }

        if (host.isFinishing) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (host.isDestroyed) {
                return
            }
        }

        activity = host

        val root =
            host.findViewById<ViewGroup>(android.R.id.content)
                ?: return

        if (composeView != null) {
            updateVisibility()
            return
        }

        try {

            val owner = GameLifecycleOwner()

            val view = ComposeView(host).apply {

                setBackgroundColor(Color.TRANSPARENT)

                setViewTreeLifecycleOwner(owner)

                setViewTreeSavedStateRegistryOwner(owner)

                /*
                 * مهم:
                 * اول attach می‌شود ولی تا وقتی Frida state نداده
                 * چیزی روی بازی نشان نمی‌دهیم.
                 */
                visibility = android.view.View.GONE
            }

            root.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            view.setContent {

                GameMenuContent(
                    currentMenu = currentMenu,
                    networkActive = networkActive,
                    helpOpen = helpOpen,
                    loginOpen = loginOpen,
                    joinNotification = joinNotification,

                    onCharacter = {
                        openCharacter()
                    },

                    onStartGame = {
                        onStartGameClicked()
                    },

                    onHelp = {
                        helpOpen = true
                        loginOpen = false
                        updateVisibility()
                    },

                    onClose = {
                        closeOverlay()
                    },

                    onLoginRegister = {
                        loginOpen = true
                        helpOpen = false
                    },

                    onPasswordChanged = {
                        passwordText = it
                    },

                    onConfirmPasswordChanged = {
                        confirmPasswordText = it
                    },

                    onLoginSubmit = {
                        onLoginRegisterSubmit()
                    }
                )
            }

            owner.resume()

            lifecycleOwner = owner
            composeView = view

            updateVisibility()

            Log.i(TAG, "GameMenu attached")

        } catch (t: Throwable) {

            Log.e(TAG, "GameMenu show failed", t)
        }
    }

    // ---------------------------------------------------------
    // HIDE
    // ---------------------------------------------------------

    @JvmStatic
    fun hide() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            activity?.runOnUiThread {
                hide()
            }

            return
        }

        try {

            lifecycleOwner?.destroy()

            composeView?.let { view ->

                (view.parent as? ViewGroup)?.removeView(view)

                view.disposeComposition()
            }

        } catch (t: Throwable) {

            Log.e(TAG, "GameMenu hide failed", t)
        }

        composeView = null
        lifecycleOwner = null
        activity = null

        helpOpen = false
        loginOpen = false
        joinNotification = false

        passwordText = ""
        confirmPasswordText = ""
    }

    // ---------------------------------------------------------
    // FRIDA -> KOTLIN STATE
    // ---------------------------------------------------------

    /**
     * Frida فقط وضعیت بازی را به UI اعلام می‌کند.
     *
     * menu:
     * 0 = MAIN
     * 1 = LAN
     * 2 = COMMUNITY
     * 3 = CHARACTER
     * 4 = SETTINGS
     * 5 = ABOUT
     *
     * networkActive:
     * true  = داخل بازی / شبکه فعال
     * false = شبکه غیرفعال
     */
    @JvmStatic
    fun setGameState(
        menu: Int,
        networkActive: Boolean
    ) {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            activity?.runOnUiThread {
                setGameState(menu, networkActive)
            }

            return
        }

        currentMenu = menu
        this.networkActive = networkActive

        /*
         * وقتی از صفحه‌های Unity خارج از MAIN/CHARACTER هستیم
         * صفحات سفارشی ما بسته می‌شوند.
         */
        if (menu != MAIN_MENU &&
            menu != CHARACTER_MENU
        ) {
            helpOpen = false
            loginOpen = false
        }

        /*
         * وقتی شبکه فعال شد تمام UI سفارشی مخفی می‌شود.
         */
        if (networkActive) {
            helpOpen = false
            loginOpen = false
            joinNotification = false
        }

        updateVisibility()

        Log.i(
            TAG,
            "STATE menu=$menu network=$networkActive"
        )
    }

    // ---------------------------------------------------------
    // START GAME EVENT
    // ---------------------------------------------------------

    /**
     * فقط event UI.
     *
     * هیچ IP / Port / اتصال شبکه‌ای اینجا نیست.
     *
     * Frida این متد را hook می‌کند.
     */
    @JvmStatic
    fun onStartGameClicked() {

        Log.i(
            TAG,
            "START_GAME_CLICKED"
        )

        showJoinNotification()
    }

    /**
     * Frida یا bridge می‌تواند این را برای نمایش
     * notification سه‌ثانیه‌ای فراخوانی کند.
     */
    @JvmStatic
    fun showJoinNotification() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            activity?.runOnUiThread {
                showJoinNotification()
            }

            return
        }

        joinNotification = true
        updateVisibility()

        activity?.window?.decorView?.postDelayed(
            {
                joinNotification = false
            },
            3000L
        )

        Log.i(
            TAG,
            "JOIN_NOTIFICATION_SHOWN"
        )
    }

    // ---------------------------------------------------------
    // CUSTOM UI
    // ---------------------------------------------------------

    @JvmStatic
    fun openCharacterFromBridge() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            activity?.runOnUiThread {
                openCharacterFromBridge()
            }

            return
        }

        openCharacter()
    }

    private fun openCharacter() {

        currentMenu = CHARACTER_MENU

        helpOpen = false
        loginOpen = false

        updateVisibility()

        Log.i(
            TAG,
            "CUSTOM_CHARACTER_OPEN"
        )
    }

    private fun closeOverlay() {

        helpOpen = false
        loginOpen = false

        /*
         * فقط UI را می‌بندیم.
         *
         * قرار نیست اینجا Unity menu یا شبکه را دستکاری کنیم.
         */
        updateVisibility()

        Log.i(
            TAG,
            "CUSTOM_OVERLAY_CLOSED"
        )
    }

    private fun onLoginRegisterSubmit() {

        /*
         * فعلاً فقط UI event است.
         * احراز هویت بعداً می‌تواند از Bridge/Frida یا
         * backend پروژه اضافه شود.
         */

        Log.i(
            TAG,
            "LOGIN_REGISTER_SUBMIT"
        )
    }

    private fun updateVisibility() {

        val view = composeView ?: return

        /*
         * اگر شبکه فعال است:
         * UI کاملاً مخفی.
         */
        if (networkActive) {

            view.visibility = android.view.View.GONE
            return
        }

        /*
         * UI سفارشی در MAIN یا CHARACTER نمایش داده می‌شود.
         */
        val visible =
            currentMenu == MAIN_MENU ||
            currentMenu == CHARACTER_MENU

        /*
         * Help/Login داخل صفحات خودمان هستند.
         */
        val showCustomPage =
            helpOpen ||
            loginOpen

        view.visibility =
            if (visible || showCustomPage)
                android.view.View.VISIBLE
            else
                android.view.View.GONE
    }
}


/*
 * ============================================================
 * ROOT CONTENT
 * ============================================================
 */

@Composable
private fun GameMenuContent(
    currentMenu: Int,
    networkActive: Boolean,
    helpOpen: Boolean,
    loginOpen: Boolean,
    joinNotification: Boolean,

    onCharacter: () -> Unit,
    onStartGame: () -> Unit,
    onHelp: () -> Unit,
    onClose: () -> Unit,
    onLoginRegister: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onLoginSubmit: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                ComposeColor.Transparent
            )
    ) {

        if (!networkActive) {

            when {

                loginOpen -> {

                    LoginPage(
                        onBack = onClose,
                        onPasswordChanged = onPasswordChanged,
                        onConfirmPasswordChanged =
                            onConfirmPasswordChanged,
                        onSubmit = onLoginSubmit
                    )
                }

                helpOpen -> {

                    HelpPage(
                        onClose = onClose
                    )
                }

                currentMenu == 0 -> {

                    MainButtons(
                        onCharacter = onCharacter,
                        onStartGame = onStartGame,
                        onHelp = onHelp
                    )
                }

                currentMenu == 3 -> {

                    CharacterPage(
                        onBack = onClose,
                        onLoginRegister = onLoginRegister
                    )
                }
            }

            if (joinNotification) {

                JoinNotification()
            }
        }
    }
}


/*
 * ============================================================
 * MAIN BUTTONS
 * ============================================================
 */

@Composable
private fun MainButtons(
    onCharacter: () -> Unit,
    onStartGame: () -> Unit,
    onHelp: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 28.dp
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        GlassButton(
            text = "Character",
            icon = Icons.Default.Person,
            onClick = onCharacter,
            green = true
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        GlassButton(
            text = "Start Game",
            icon = Icons.Default.PlayArrow,
            onClick = onStartGame,
            green = true
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        GlassButton(
            text = "Help",
            icon = Icons.Default.QuestionMark,
            onClick = onHelp,
            green = false
        )
    }
}


/*
 * ============================================================
 * GLASS BUTTON
 * ============================================================
 */

@Composable
private fun GlassButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    green: Boolean
) {

    val shape =
        RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier
            .width(150.dp)
            .height(54.dp)
            .clickable {
                onClick()
            }
            .border(
                width = 1.dp,
                color =
                    if (green) {
                        ComposeColor(0x6699D8AF)
                    } else {
                        ComposeColor(0x66777777)
                    },
                shape = shape
            ),
        shape = shape,
        color =
            if (green) {
                ComposeColor(0xC51D3A2A)
            } else {
                ComposeColor(0xC51A1D21)
            },
        elevation = 7.dp
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = ComposeColor.White,
                modifier = Modifier.size(21.dp)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = text,
                color = ComposeColor.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/*
 * ============================================================
 * CHARACTER PAGE
 * ============================================================
 */

@Composable
private fun CharacterPage(
    onBack: () -> Unit,
    onLoginRegister: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        GlassPanel(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ComposeColor.White
                    )
                }

                Text(
                    text = "Character",
                    color = ComposeColor.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(
                        Alignment.TopCenter
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 70.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Status(
                        title = "Role",
                        value = "Null"
                    )

                    Status(
                        title = "Money",
                        value = "Null"
                    )
                }

                OutlinedButton(
                    onClick = onLoginRegister,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 125.dp)
                        .width(205.dp)
                        .height(48.dp),
                    border = BorderStroke(
                        1.dp,
                        ComposeColor(0x77FFFFFF)
                    ),
                    colors =
                        ButtonDefaults
                            .outlinedButtonColors(
                                backgroundColor =
                                    ComposeColor(
                                        0x401D2721
                                    ),
                                contentColor =
                                    ComposeColor.White
                            ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Login / Register"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 105.dp)
                ) {

                    CharacterCross()
                }
            }
        }
    }
}


@Composable
private fun Status(
    title: String,
    value: String
) {

    Column {

        Text(
            text = title,
            color = ComposeColor(0xBFFFFFFF),
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = value,
            color = ComposeColor.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/*
 * ============================================================
 * CHARACTER CROSS
 * ============================================================
 */

@Composable
private fun CharacterCross() {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CharacterItem()

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            CharacterItem()
            CharacterItem()
            CharacterItem()
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        CharacterItem()
    }
}


@Composable
private fun CharacterItem() {

    Surface(
        modifier = Modifier.size(
            width = 100.dp,
            height = 54.dp
        ),
        shape = RoundedCornerShape(14.dp),
        color = ComposeColor(0x5A1D2721),
        border = BorderStroke(
            1.dp,
            ComposeColor(0x556A9A7A)
        ),
        elevation = 4.dp
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Character",
                color = ComposeColor.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


/*
 * ============================================================
 * LOGIN / REGISTER
 * ============================================================
 */

@Composable
private fun LoginPage(
    onBack: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {

    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(34.dp),
        contentAlignment = Alignment.Center
    ) {

        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    IconButton(
                        onClick = onBack,
                        modifier =
                            Modifier.align(Alignment.TopStart)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back",
                            tint =
                                ComposeColor.White
                        )
                    }

                    Text(
                        text = "Login / Register",
                        color = ComposeColor.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onPasswordChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Password")
                    },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        onConfirmPasswordChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Confirm password")
                    },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation()
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "Role: Null",
                    color = ComposeColor(0xBFFFFFFF),
                    fontSize = 14.sp
                )

                Text(
                    text = "Money: Null",
                    color = ComposeColor(0xBFFFFFFF),
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                OutlinedButton(
                    onClick = onSubmit,
                    modifier = Modifier
                        .width(180.dp)
                        .height(48.dp),
                    border = BorderStroke(
                        1.dp,
                        ComposeColor(0x7799D8AF)
                    ),
                    colors =
                        ButtonDefaults
                            .outlinedButtonColors(
                                backgroundColor =
                                    ComposeColor(
                                        0x401D3A2A
                                    ),
                                contentColor =
                                    ComposeColor.White
                            ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Continue"
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * HELP
 * ============================================================
 */

@Composable
private fun HelpPage(
    onClose: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(34.dp),
        contentAlignment = Alignment.Center
    ) {

        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(
                        Alignment.TopEnd
                    )
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Close,
                        contentDescription =
                            "Close",
                        tint = ComposeColor.White
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "به زودی",
                        color = ComposeColor.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Help",
                        color = ComposeColor(0xAAFFFFFF),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * JOIN NOTIFICATION
 * ============================================================
 */

@Composable
private fun JoinNotification() {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 102.dp
            )
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        color = ComposeColor(0xE51A1D20),
        border = BorderStroke(
            1.dp,
            ComposeColor(0x4477AA88)
        ),
        elevation = 8.dp
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "درحال پیوستن به سرور...",
                color = ComposeColor.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


/*
 * ============================================================
 * GLASS PANEL
 * ============================================================
 */

@Composable
private fun GlassPanel(
    modifier: Modifier,
    content: @Composable () -> Unit
) {

    val shape =
        RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = ComposeColor(0xC5101418),
        border = BorderStroke(
            1.dp,
            ComposeColor(0x4499AA99)
        ),
        elevation = 10.dp
    ) {

        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ComposeColor(0x1DFFFFFF),
                            ComposeColor(0x05FFFFFF)
                        )
                    )
                )
        ) {

            content()
        }
    }
}


/*
 * ============================================================
 * LIFECYCLE OWNER
 * ============================================================
 */

private class GameLifecycleOwner :
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry =
        LifecycleRegistry(this)

    private val savedStateController =
        SavedStateRegistryController.create(this)

    init {

        savedStateController.performAttach()

        savedStateController.performRestore(null)

        lifecycleRegistry.currentState =
            Lifecycle.State.CREATED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() =
            savedStateController.savedStateRegistry

    fun resume() {

        lifecycleRegistry.currentState =
            Lifecycle.State.RESUMED
    }

    fun destroy() {

        lifecycleRegistry.currentState =
            Lifecycle.State.DESTROYED
    }
}