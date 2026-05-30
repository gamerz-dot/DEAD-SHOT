package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// High contrast military/tactical color themes
object DeadShotTheme {
    val DarkSlate = Color(0xFF0F1013)
    val CarbonBlack = Color(0xFF16181C)
    val LaserGreen = Color(0xFF39FF14)
    val TacticalRed = Color(0xFFFF3B30)
    val MetallicSilver = Color(0xFFE2E8F0)
    val NeonBlue = Color(0xFF00E5FF)
    val GoldAccent = Color(0xFFFFCC00)
    val DarkOverlay = Color(0xCC08090C)
}

@Composable
fun DeadShotGameApp(viewModel: DeadShotViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val context = LocalContext.current

    // Force strict 16:9 landscape aspect ratio box in the center, 
    // styled with technical sci-fi carbon layout frames if the physical device behaves differently!
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val targetRatio = 16f / 9f

        val containerWidth: androidx.compose.ui.unit.Dp
        val containerHeight: androidx.compose.ui.unit.Dp

        if (screenWidth / screenHeight > targetRatio) {
            containerHeight = screenHeight
            containerWidth = screenHeight * targetRatio
        } else {
            containerWidth = screenWidth
            containerHeight = screenWidth / targetRatio
        }

        Box(
            modifier = Modifier
                .width(containerWidth)
                .height(containerHeight)
                .clip(RoundedCornerShape(0.dp))
                .border(2.dp, Color(0xFF232730))
                .shadow(24.dp)
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(700, easing = LinearOutSlowInEasing)
            ) { screen ->
                when (screen) {
                    ActiveScreen.SPLASH -> DeadShotSplashScreen()
                    ActiveScreen.LOGIN -> DeadShotLoginScreen(viewModel)
                    ActiveScreen.LOBBY -> DeadShotLobbyScreen(viewModel)
                    ActiveScreen.ARENA -> DeadShotArenaScreen(viewModel)
                }
            }
        }
    }
}

// 1. SPLASH SCREEN - CHANNEL 4 PROFILE ASSEMBLAGE
@Composable
fun DeadShotSplashScreen() {
    var assembleProgress by remember { mutableStateOf(0f) }
    var scaleLogo by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        // Animate the assembly of Channel 4's stylized 3D geometric block puzzle
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2200, easing = FastOutSlowInEasing)
        ) { value, _ ->
            assembleProgress = value
        }
        // Smooth breath zoom at completion
        animate(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = tween(500, easing = LinearOutSlowInEasing)
        ) { value, _ ->
            scaleLogo = value
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeadShotTheme.DarkSlate),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High end tactical representation of Channel 4's blocks
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleLogo),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing British Channel 4 styled blocks assembling based on assembleProgress
                    // Blocks represent components of the number "4"
                    // Offset values slide back down from surrounding boundaries as progress increases
                    val slideOffsetTop = (1f - assembleProgress) * -120f
                    val slideOffsetBottom = (1f - assembleProgress) * 120f
                    val slideOffsetLeft = (1f - assembleProgress) * -150f
                    val slideOffsetRight = (1f - assembleProgress) * 150f

                    // 1. Left Vertical Pillar Segment (Laser Green glow)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(DeadShotTheme.LaserGreen, Color(0xFF0F9D58))
                        ),
                        topLeft = Offset(w * 0.22f + slideOffsetLeft, h * 0.15f + slideOffsetTop),
                        size = Size(w * 0.12f, h * 0.55f)
                    )

                    // 2. Middle Horizontal Bar Segment (Tactical Red accent)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(DeadShotTheme.TacticalRed, Color(0xFFB30000))
                        ),
                        topLeft = Offset(w * 0.35f, h * 0.45f + slideOffsetBottom),
                        size = Size(w * 0.42f, h * 0.12f)
                    )

                    // 3. Floating Right Angled Blocks (Steel silver / Gold)
                    rotate(15f, pivot = Offset(w * 0.65f, h * 0.3f)) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(DeadShotTheme.GoldAccent, Color(0xFFD4AF37))
                            ),
                            topLeft = Offset(w * 0.61f + slideOffsetRight, h * 0.15f + slideOffsetTop),
                            size = Size(w * 0.14f, h * 0.38f)
                        )
                    }

                    // 4. Base Block Segment (Shadow Overlay)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(DeadShotTheme.MetallicSilver, Color(0xFF718096))
                        ),
                        topLeft = Offset(w * 0.22f + slideOffsetLeft, h * 0.72f + slideOffsetBottom),
                        size = Size(w * 0.53f, h * 0.10f)
                    )

                    // Glow sweep lines
                    val glowY = (assembleProgress * h * 1.5f) - (h * 0.25f)
                    if (assembleProgress > 0.3f) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(0f, glowY),
                            end = Offset(w, glowY - 40f),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CHANNEL 4 PROFILE PRESENTING",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DeadShotTheme.MetallicSilver.copy(alpha = 0.5f),
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "DEAD SHOT",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color.White,
                    letterSpacing = 10.sp
                ),
                modifier = Modifier.offset(y = (20f * (1f - assembleProgress)).dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Infinite charging/loading bar line
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .background(Color(0xFF22252A))
                    .clip(CircleShape)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val progressX by infiniteTransition.animateFloat(
                    initialValue = -0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.35f)
                        .align(Alignment.CenterStart)
                        .offset(x = (180.dp * progressX))
                        .background(DeadShotTheme.LaserGreen)
                )
            }
        }
    }
}

// 2. LOGIN SCREEN - SECURE GUEST, GOOGLE & FB PORTALS
@Composable
fun DeadShotLoginScreen(viewModel: DeadShotViewModel) {
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val isGuestUser by viewModel.isGuestUser.collectAsState()
    val showGoogleAccountPicker by viewModel.showGoogleAccountPicker.collectAsState()
    val googleDriveSyncState by viewModel.googleDriveSyncState.collectAsState()
    val showProfileNameDialog by viewModel.showProfileNameDialog.collectAsState()
    val folderPathStatus by viewModel.folderPathStatus.collectAsState()
    val profileName by viewModel.profileName.collectAsState()
    val playerBId by viewModel.playerBId.collectAsState()
    val googleAccounts by viewModel.googleAccounts.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Smooth entry animations for the main visual card
    var startButtonRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(profileName, playerBId) {
        if (playerBId > 0) {
            startButtonRevealed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeadShotTheme.CarbonBlack)
    ) {
        // High fidelity background canvas drawing rain particles and battlefield searchlights
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect {
                // Background dark radial gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF22252E), DeadShotTheme.CarbonBlack),
                        center = center,
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = center
                )

                // Atmospheric military green/red beacons
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeadShotTheme.TacticalRed.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.2f),
                        radius = size.width * 0.4f
                    ),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.1f, size.height * 0.2f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeadShotTheme.LaserGreen.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.8f),
                        radius = size.width * 0.4f
                    ),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.9f, size.height * 0.8f)
                )
            }
        }

        // Tactical Overlay HUD border frames
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Brand presentation and visual tagline
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DeadShotTheme.TacticalRed, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "VERSION 2.0.6",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.35f),
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DEAD SHOT",
                    style = TextStyle(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tactical Combat Simulator. Features Pakistan city sandboxes including Karachi shipping docks, Lahore fortress, and Islamabad Margalla mountain heights with simulated dynamic monsoons.",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = DeadShotTheme.MetallicSilver.copy(alpha = 0.5f),
                        lineHeight = 16.sp
                    )
                )

                if (playerBId > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color(0xFF1E2129),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "AUTH: ${profileName.uppercase()}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DeadShotTheme.LaserGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "BID INDEX: DS-$playerBId",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = folderPathStatus,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.4f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Right Side: Beautiful container carrying auth cards or Start launcher
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = !startButtonRevealed,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SECURE PROTOCOL ACCESS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // 1. GUEST KEY ENTRY
                        AuthMethodCard(
                            title = "GUEST OPERATOR",
                            desc = "Frees space folder creation (com.dead.shot)",
                            accentColor = DeadShotTheme.MetallicSilver,
                            onClick = { viewModel.onGuestLoginClicked() },
                            modifier = Modifier.testTag("btn_auth_guest")
                        )

                        // 2. GOOGLE SECURE DRIVE LINK
                        AuthMethodCard(
                            title = "GOOGLE ACCOUNT",
                            desc = "Upload profile data syncing on Drive",
                            accentColor = DeadShotTheme.NeonBlue,
                            onClick = { viewModel.onGoogleLoginClicked(null) },
                            modifier = Modifier.testTag("btn_auth_google")
                        )

                        // 3. FACEBOOK
                        AuthMethodCard(
                            title = "FACEBOOK DIRECT",
                            desc = "Social network direct portal link",
                            accentColor = Color(0xFF1877F2),
                            onClick = { viewModel.performFacebookLogin() },
                            modifier = Modifier.testTag("btn_auth_facebook")
                        )
                    }
                }

                AnimatedVisibility(
                    visible = startButtonRevealed,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Flashing start game button
                        val infiniteTransition = rememberInfiniteTransition()
                        val glowingAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Button(
                            onClick = { viewModel.navigateTo(ActiveScreen.LOBBY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeadShotTheme.LaserGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(56.dp)
                                .shadow(elevation = 12.dp, spotColor = DeadShotTheme.LaserGreen, ambientColor = DeadShotTheme.LaserGreen, clip = false)
                                .alpha(glowingAlpha)
                                .testTag("btn_click_to_start"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "CLICK TO START",
                                fontSize = 16.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 3.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(
                            onClick = { startButtonRevealed = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "CHANGE PROFILE ACCOUNT",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // GUEST STORAGE PERMISSION DIALOG
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionDialog() },
            title = {
                Text(
                    text = "Storage Permission Required",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Dead shot needs access to write configuration files into your local storage database directory. This creates a folder named \"com.dead.shot\" to persist your guest credentials.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.grantStoragePermission(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.LaserGreen),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("ALLOW", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissPermissionDialog() }
                ) {
                    Text("DENY", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1C1E24),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("dialog_storage_permission")
        )
    }

    // GOOGLE ACCOUNTS PICKER POPUP
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGooglePicker() },
            title = {
                Text(
                    text = "Sign in with Google",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose an account to continue with Dead shot Google Drive backup service:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    googleAccounts.forEach { email ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2E38)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectGoogleAccount(email) },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = email.take(1).uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = email.substringBefore("@"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = email,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGooglePicker() }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E212A),
            shape = RoundedCornerShape(8.dp)
        )
    }

    // GOOGLE DRIVE SYNCING MODAL
    if (googleDriveSyncState == "SYNCING") {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = DeadShotTheme.NeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CONTACTING GOOGLE PLUGINS DRIVE...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generating remote player file configurations",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color(0xFF1A1C23),
            shape = RoundedCornerShape(8.dp)
        )
    }

    // CUSTOM NAME MAKER DIALOG
    if (showProfileNameDialog) {
        var textInput by remember { mutableStateOf(TextFieldValue(profileName)) }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Name your Profile",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Customize your profile name. This name represents your avatar handle inside online tactical dashboards first-person lobbies:",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeadShotTheme.NeonBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF262933),
                            unfocusedContainerColor = Color(0xFF1C1D24),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("Profile Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("input_profile_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitProfileName(textInput.text) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.NeonBlue),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("REGISTRATION", color = Color.Black)
                }
            },
            containerColor = Color(0xFF1E212A),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun AuthMethodCard(
    title: String,
    desc: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D24)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(46.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// 3. LOBBY SCREEN - TOKYO NIGHTS & ANIME MOTION SCENERY
@Composable
fun DeadShotLobbyScreen(viewModel: DeadShotViewModel) {
    val profileName by viewModel.profileName.collectAsState()
    val playerBId by viewModel.playerBId.collectAsState()
    val rubies by viewModel.rubies.collectAsState()
    val selectedMap by viewModel.selectedMap.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val selectedWeapon by viewModel.selectedWeapon.collectAsState()

    val mailList by viewModel.mailList.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val friendsList by viewModel.friendsList.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Navigation Tab Selection of Right sidebar panel
    var selectedRightTab by remember { mutableStateOf("FRIENDS") } // "MAIL", "CHAT", "FRIENDS"

    // Text box state for localized team chatting
    var customChatInput by remember { mutableStateOf(TextFieldValue("")) }

    // Parallax background coordinates
    val backgroundTranslation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // Continuous slow sliding movement representing camera scrolling down/left neon Tokyo streets
        backgroundTranslation.animateTo(
            targetValue = -350f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeadShotTheme.DarkSlate)
    ) {
        // HIGH PERFORMANCE PROCEDURAL TOKYO SKYLINE & WEATHER ANIMATION
        // Includes background anime landscape with pink bloom clouds/sky starry overlays
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Let users touch/drag lobby background and experience parallax reaction!
                }
        ) {
            val w = size.width
            val h = size.height

            clipRect {
                // Background twilight skies (Dark violet-indigo grading)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF03030F), Color(0xFF10122B))
                    )
                )

                // Twinkling neon stars
                val starRandom = Random(99)
                for (i in 0..50) {
                    val starX = starRandom.nextFloat() * w
                    val starY = starRandom.nextFloat() * (h * 0.5f)
                    val starAlpha = starRandom.nextFloat() * 0.7f + 0.3f
                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        radius = starRandom.nextFloat() * 1.5f + 0.8f,
                        center = Offset(starX, starY)
                    )
                }

                val offsetParallax = backgroundTranslation.value

                // Draw neon buildings (Skyscrapers layers)
                val buildingCount = 8
                val buildRand = Random(22)
                for (i in 0 until buildingCount) {
                    val scaleFactor = buildRand.nextFloat() * 0.5f + 0.5f
                    val bW = (w * 0.15f + buildRand.nextFloat() * 100f) * scaleFactor
                    val bH = (h * 0.4f + buildRand.nextFloat() * h * 0.3f)
                    val bX = (i * w * 0.14f + offsetParallax * 0.3f)

                    // Draw silhouette blocks
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF12142B).copy(alpha = 0.95f), DeadShotTheme.DarkSlate)
                        ),
                        topLeft = Offset(bX, h - bH),
                        size = Size(bW, bH)
                    )

                    // Draw window tiny grids inside building silhouettes
                    val windowsRows = (bH / 20f).toInt().coerceAtMost(10)
                    val windowsCols = (bW / 12f).toInt().coerceAtMost(6)
                    for (r in 1 until windowsRows) {
                        for (c in 1 until windowsCols) {
                            if (buildRand.nextFloat() > 0.4f) {
                                val winColor = listOf(
                                    Color(0xFF8FFBFF), // Cyan
                                    Color(0xFFFF9FF3), // Pink
                                    DeadShotTheme.GoldAccent // Warm yellow
                                ).random(buildRand)
                                drawRect(
                                    color = winColor.copy(alpha = 0.35f),
                                    topLeft = Offset(bX + c * 12f + 4f, h - bH + r * 20f + 4f),
                                    size = Size(4f, 6f)
                                )
                            }
                        }
                    }
                }

                // Draw iconic TOKYO RED TOWER silhouette with glowing beacon beams
                val towerX = w * 0.75f + offsetParallax * 0.15f
                val towerBaseW = 40f
                val towerH = 180f
                // Base truss
                drawRect(
                    color = Color(0xFFFF4D4D).copy(alpha = 0.7f),
                    topLeft = Offset(towerX - towerBaseW / 2, h - towerH),
                    size = Size(towerBaseW * 0.15f, towerH)
                )
                // Draw glowing apex pulse
                drawCircle(
                    color = Color.Red,
                    radius = 5f,
                    center = Offset(towerX - towerBaseW / 2 + 3f, h - towerH)
                )

                // Anime Pilot representation floating silhouette (overlay matching input_file_0.png style)
                // Drew a cyber aviator flying floating silhouette in the sky on the left side
                val pilotX = w * 0.2f
                val pilotY = h * 0.35f
                val path = Path().apply {
                    moveTo(pilotX, pilotY)
                    quadraticBezierTo(pilotX + 40f, pilotY - 30f, pilotX + 110f, pilotY + 10f) // body curve
                    quadraticBezierTo(pilotX + 130f, pilotY + 30f, pilotX + 180f, pilotY + 40f) // extended coat tail
                    quadraticBezierTo(pilotX + 150f, pilotY + 50f, pilotX + 100f, pilotY + 50f)
                    quadraticBezierTo(pilotX + 50f, pilotY + 60f, pilotX, pilotY + 12f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(DeadShotTheme.TacticalRed.copy(alpha = 0.4f), Color.Transparent),
                        start = Offset(pilotX, pilotY),
                        end = Offset(pilotX + 140f, pilotY + 50f)
                    )
                )

                // Draw glowing red stream flares of coat jets! (anime flight motion lines)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeadShotTheme.TacticalRed.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(pilotX + 25f, pilotY + 20f),
                        radius = 28f
                    ),
                    radius = 28f,
                    center = Offset(pilotX + 25f, pilotY + 20f)
                )

                drawLine(
                    color = DeadShotTheme.TacticalRed.copy(alpha = 0.8f),
                    start = Offset(pilotX + 20f, pilotY + 22f),
                    end = Offset(pilotX - 100f, pilotY - 20f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = DeadShotTheme.TacticalRed.copy(alpha = 0.5f),
                    start = Offset(pilotX + 28f, pilotY + 30f),
                    end = Offset(pilotX - 130f, pilotY + 10f),
                    strokeWidth = 1.5f
                )
            }
        }

        // AMBIENT PINK NEON DRIZZLE RAIN OVERLAY
        val infiniteTransition = rememberInfiniteTransition()
        val rainTime by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rSeed = Random(888)
            for (i in 0..15) {
                val startX = rSeed.nextFloat() * size.width
                val driftY = (rainTime * size.height) + (rSeed.nextFloat() * 100f)
                drawLine(
                    color = Color(0xFFFF9FF3).copy(alpha = 0.3f),
                    start = Offset(startX, driftY % size.height),
                    end = Offset(startX - 15f, (driftY + 45f) % size.height),
                    strokeWidth = 2f
                )
            }
        }

        // LOBBY INTERACTIVE INTERFACE ROOT GRID
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR: PROFILE BADGE & CURRENCY PANEL (Left to Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // PROFILE WIDGET (Left corner)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xD9101217), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Profile Helmet Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF20232A), CircleShape)
                            .border(1.dp, DeadShotTheme.LaserGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profileName.take(2).uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeadShotTheme.LaserGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = profileName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "BId: $playerBId",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // CENTRED GAME NAME BANNER WITH GLOW accent
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DEAD SHOT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "LOBBY BATTLEGROUND GATE",
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DeadShotTheme.LaserGreen,
                        letterSpacing = 1.sp
                    )
                }

                // CURRENCY PANEL (Rubies tracker)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xD9101217), RoundedCornerShape(8.dp))
                        .border(1.dp, DeadShotTheme.GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("rubies_panel")
                ) {
                    val gemTransition = rememberInfiniteTransition()
                    val rubyScale by gemTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    // Stylized Ruby Vector Gem
                    Canvas(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(rubyScale)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width * 0.5f, 0f)
                            lineTo(size.width, size.height * 0.35f)
                            lineTo(size.width * 0.5f, size.height)
                            lineTo(0f, size.height * 0.35f)
                            close()
                        }
                        drawPath(path, brush = Brush.linearGradient(colors = listOf(Color(0xFFFF5252), Color(0xFFFF1744))))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${"%,d".format(rubies)} RUBY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "CREDITS",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // MIDDLE SECTION: MAP SELECTOR & SETTINGS (LEFT) / INTERACTIVE PANELS (RIGHT)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                // LEFT PORTION: Dynamic Selection Deck (Maps, Guns, Weathers)
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // CONFIG CARDS ROWS (MAPS, WEAPONS, WEATHERS)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. REGIONAL MAP SELECTION DECK
                        Column {
                            Text(
                                text = "ARENA TARGET LOCATION (PAKISTAN)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PakistanMap.values().forEach { map ->
                                    MapConfigChoiceCard(
                                        label = map.label,
                                        urdu = when(map) {
                                            PakistanMap.KARACHI -> "کراچی"
                                            PakistanMap.LAHORE -> "لاہور"
                                            PakistanMap.ISLAMABAD -> "اسلام آباد"
                                        },
                                        isSelected = selectedMap == map,
                                        onSelected = { viewModel.selectMap(map) },
                                        modifier = Modifier.weight(1f).testTag("card_map_${map.name}")
                                    )
                                }
                            }
                        }

                        // 2. THE WEAPONS ARSENAL SELECTION
                        Column {
                            Text(
                                text = "TACTICAL FIREPOWER LOADOUT",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                WeaponType.values().forEach { weapon ->
                                    WeaponConfigChoiceCard(
                                        label = weapon.label.substringBefore(" "),
                                        sub = "Ammo: ${weapon.ammoMax}",
                                        damage = weapon.damage,
                                        isSelected = selectedWeapon == weapon,
                                        onSelected = { viewModel.selectWeapon(weapon) },
                                        modifier = Modifier.weight(1f).testTag("card_weapon_${weapon.name}")
                                    )
                                }
                            }
                        }

                        // 3. THE DYNAMIC WEATHER SELECTION DECK
                        Column {
                            Text(
                                text = "ATMOSPHERIC COMBAT ENVIRONMENT",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                WeatherType.values().forEach { weather ->
                                    WeatherConfigChoiceCard(
                                        label = weather.label,
                                        desc = weather.desc.substringBefore(","),
                                        isSelected = selectedWeather == weather,
                                        onSelected = { viewModel.selectWeather(weather) },
                                        modifier = Modifier.weight(1f).testTag("card_weather_${weather.name}")
                                    )
                                }
                            }
                        }
                    }

                    // TACTICAL SUM OF SELECTIONS INFO BOX
                    Surface(
                        color = Color(0xD90E1015),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(DeadShotTheme.LaserGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WARZONE CONFIGURED: ${selectedMap.locationName.uppercase()} // CLIMATE: ${selectedWeather.label.uppercase()} // ACTIVE FIREARM: ${selectedWeapon.label.uppercase()}",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DeadShotTheme.MetallicSilver.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // RIGHT PORTION: Tabbed Communication Dashboard (MAIL, CHAT, FRIENDS list)
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .background(Color(0xD90E1015), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    // TAB NAVIGATION ROW (MAIL / CO-OP TEAM / FRIEND list)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabHeaderItem(
                            label = "MAIL",
                            badgeCount = mailList.count { !it.isClaimed },
                            isSelected = selectedRightTab == "MAIL",
                            onClick = { selectedRightTab = "MAIL" },
                            modifier = Modifier.weight(1f)
                        )
                        TabHeaderItem(
                            label = "TEAM",
                            hasIndicator = true,
                            isSelected = selectedRightTab == "CHAT",
                            onClick = { selectedRightTab = "CHAT" },
                            modifier = Modifier.weight(1f)
                        )
                        TabHeaderItem(
                            label = "FRIEND",
                            badgeCount = friendsList.size,
                            isSelected = selectedRightTab == "FRIENDS",
                            onClick = { selectedRightTab = "FRIENDS" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // TAB VIEWPORTS CONFIGURATION
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedRightTab) {
                            "MAIL" -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxSize().testTag("mail_list")
                                ) {
                                    items(mailList) { mail ->
                                        MailRowWidget(
                                            mail = mail,
                                            onClaim = { viewModel.claimMailBonus(mail.id) }
                                        )
                                    }
                                }
                            }
                            "CHAT" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .testTag("team_chat_view"),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        reverseLayout = true
                                    ) {
                                        items(chatMessages.reversed()) { chat ->
                                            ChatRowWidget(chat)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Custom fast-chat trigger inputs
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    ) {
                                        listOf("Ready Up!", "Karachi Port Go!", "Raining heavily").forEach { quickText ->
                                            Text(
                                                text = quickText,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                color = DeadShotTheme.LaserGreen,
                                                modifier = Modifier
                                                    .background(Color(0xFF20232B), RoundedCornerShape(4.dp))
                                                    .border(0.5.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .clickable { viewModel.sendChatMessage(quickText) }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Raw keyboard transmission inputs
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E2129), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        BasicTextField(
                                            value = customChatInput,
                                            onValueChange = { customChatInput = it },
                                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                            keyboardActions = KeyboardActions(onSend = {
                                                viewModel.sendChatMessage(customChatInput.text)
                                                customChatInput = TextFieldValue("")
                                            }),
                                            modifier = Modifier.weight(1f).testTag("chat_input_field"),
                                            decorationBox = { innerTextField ->
                                                if (customChatInput.text.isEmpty()) {
                                                    Text("Send team message...", color = Color.Gray, fontSize = 10.sp)
                                                }
                                                innerTextField()
                                            }
                                        )
                                        Text(
                                            text = "SEND",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeadShotTheme.NeonBlue,
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.sendChatMessage(customChatInput.text)
                                                    customChatInput = TextFieldValue("")
                                                }
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                            "FRIENDS" -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxSize().testTag("lobby_friends_list")
                                ) {
                                    items(friendsList) { friend ->
                                        FriendRowWidget(
                                            name = friend["name"] ?: "",
                                            bId = friend["bId"] ?: "",
                                            status = friend["status"] ?: "",
                                            weapon = friend["weapon"] ?: "",
                                            onInvite = {
                                                Toast.makeText(context, "Invited ${friend["name"]} to squad!", Toast.LENGTH_SHORT).show()
                                                viewModel.sendChatMessage("Hey ${friend["name"]}, join team!")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM BAR: START GAME LAUNCH BINDER PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return parameters back
                OutlinedButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.LOGIN) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("LOGOUT", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // GIANT BATTLEGROUND ACTION STARTER BUTTON
                Button(
                    onClick = {
                        viewModel.setupArenaTargets()
                        viewModel.navigateTo(ActiveScreen.ARENA)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeadShotTheme.LaserGreen
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(elevation = 8.dp, spotColor = DeadShotTheme.LaserGreen, ambientColor = DeadShotTheme.LaserGreen)
                        .testTag("btn_launch_sandbox")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "DEPLOY INTO ${selectedMap.label.uppercase()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[ FPS MODE ]",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// LOBBY CARD HELPERS
@Composable
fun MapConfigChoiceCard(
    label: String,
    urdu: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1C2D24) else Color(0xFF14161C)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.clickable(onClick = onSelected)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = urdu,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DeadShotTheme.LaserGreen else Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WeaponConfigChoiceCard(
    label: String,
    sub: String,
    damage: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF132B3A) else Color(0xFF14161C)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) DeadShotTheme.NeonBlue else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.clickable(onClick = onSelected)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) DeadShotTheme.NeonBlue else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$sub // DMG: $damage",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeatherConfigChoiceCard(
    label: String,
    desc: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF241C1A) else Color(0xFF14161C)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) DeadShotTheme.TacticalRed else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.clickable(onClick = onSelected)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DeadShotTheme.TacticalRed else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TabHeaderItem(
    label: String,
    badgeCount: Int = 0,
    hasIndicator: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xFF1E212A) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(DeadShotTheme.TacticalRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (hasIndicator) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(DeadShotTheme.LaserGreen, CircleShape)
                )
            }
        }
    }
}

@Composable
fun MailRowWidget(mail: SystemMail, onClaim: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D24)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (mail.isClaimed) Color.Gray else DeadShotTheme.GoldAccent,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mail.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!mail.isClaimed) {
                    Text(
                        text = "+${mail.rubyBonus} 💎 CLAIM",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DeadShotTheme.GoldAccent,
                        modifier = Modifier
                            .background(Color(0xFF26231C), RoundedCornerShape(4.dp))
                            .border(0.5.dp, DeadShotTheme.GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable(onClick = onClaim)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = "CLAIMED",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mail.text,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun ChatRowWidget(chat: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (chat.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (chat.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = chat.senderName,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (chat.isUser) DeadShotTheme.NeonBlue else DeadShotTheme.LaserGreen
            )
        }
        Box(
            modifier = Modifier
                .padding(vertical = 1.dp)
                .background(
                    if (chat.isUser) Color(0xFF1E2833) else Color(0xFF1B1D24),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = chat.content,
                fontSize = 9.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun FriendRowWidget(
    name: String,
    bId: String,
    status: String,
    weapon: String,
    onInvite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D24)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (status.startsWith("In-Match")) DeadShotTheme.TacticalRed 
                            else if (status == "Lobby") DeadShotTheme.LaserGreen 
                            else Color.Gray,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = status,
                        fontSize = 8.sp,
                        color = Color.LightGray.copy(alpha = 0.6f)
                    )
                }
            }

            if (status == "Lobby") {
                Text(
                    text = "INVITE",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    modifier = Modifier
                        .background(DeadShotTheme.LaserGreen, RoundedCornerShape(3.dp))
                        .clickable(onClick = onInvite)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            } else {
                Text(
                    text = weapon,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }
        }
    }
}

// 4. ARENA SCREEN - COMBAT TACTICAL ACTION SANDBOX
@Composable
fun DeadShotArenaScreen(viewModel: DeadShotViewModel) {
    val selectedMap by viewModel.selectedMap.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val selectedWeapon by viewModel.selectedWeapon.collectAsState()
    val activeCameraAngle by viewModel.activeCameraAngle.collectAsState()

    val currentAmmo by viewModel.currentAmmo.collectAsState()
    val isReloading by viewModel.isReloading.collectAsState()
    val isFiring by viewModel.isFiring.collectAsState()
    val recoilOffset by viewModel.recoilOffset.collectAsState()
    val isScoped by viewModel.isScoped.collectAsState()
    val scenicTargets by viewModel.scenicTargets.collectAsState()
    val score by viewModel.score.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Impact notifications floating message popup
    var combatFeedbackMessage by remember { mutableStateOf("") }
    var feedbackTriggerToggle by remember { mutableStateOf(false) }

    // Touch/Aim parameters
    var crosshairX by remember { mutableStateOf(400f) }
    var crosshairY by remember { mutableStateOf(240f) }

    // Particle rain / wind loop
    val simulationFrame = remember { mutableStateOf(0) }
    LaunchedEffect(scenicTargets) {
        while (true) {
            delay(30)
            simulationFrame.value += 1
            // Perform automatic targets horizontal drifting if Islamabad hills active
            viewModel.updateMovingTargets(800f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Move aiming crosshair offset smoothly inside valid dimensions
                        crosshairX = (crosshairX + dragAmount.x).coerceIn(40f, 760f)
                        crosshairY = (crosshairY + dragAmount.y).coerceIn(40f, 380f)
                    }
                )
            }
    ) {
        // MAIN VECTOR GROUND ARENAS - DRAWS MAP ELEMENTS WITH DYNAMIC PARALLAX WEATHER SHADERS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            clipRect {
                // Background Sky coloring depending on Maps
                val skyColor = when (selectedMap) {
                    PakistanMap.KARACHI -> Brush.verticalGradient(listOf(Color(0xFF131720), Color(0xFF202B3B)))
                    PakistanMap.LAHORE -> Brush.verticalGradient(listOf(Color(0xFF2C1916), Color(0xFF3F2723)))
                    PakistanMap.ISLAMABAD -> Brush.verticalGradient(listOf(Color(0xFF0F1B12), Color(0xFF1E3324)))
                }
                drawRect(brush = skyColor)

                // Render customized landscape depending on Map chosen
                when (selectedMap) {
                    PakistanMap.KARACHI -> {
                        // Drawing Karachi Ports docks (Ocean and stacked colorful cargo blocks)
                        val portSeaY = h * 0.55f
                        drawRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF0D253F), Color(0xFF04101D))),
                            topLeft = Offset(0f, portSeaY),
                            size = Size(w, h - portSeaY)
                        )

                        // Draw giant mechanical cranes
                        val craneColor = Color(0xFFDFE2E6).copy(alpha = 0.15f)
                        drawLine(craneColor, Offset(w * 0.1f, portSeaY), Offset(w * 0.18f, h * 0.2f), strokeWidth = 10f)
                        drawLine(craneColor, Offset(w * 0.18f, h * 0.2f), Offset(w * 0.45f, h * 0.22f), strokeWidth = 6f)
                        drawLine(craneColor, Offset(w * 0.35f, h * 0.22f), Offset(w * 0.35f, portSeaY - 40f), strokeWidth = 2f) // cabling

                        // Static large steel metal buildings background
                        drawRect(Color(0xFF161A22), topLeft = Offset(w * 0.5f, portSeaY - 140f), size = Size(w * 0.4f, 140f))
                        drawRect(Color(0xFF101217), topLeft = Offset(w * 0.55f, portSeaY - 80f), size = Size(w * 0.3f, 80f))
                    }
                    PakistanMap.LAHORE -> {
                        // Drawing Lahore ancient Mughall brick walls and watchtowers
                        val wallY = h * 0.5f
                        drawRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF331C18), Color(0xFF26120F))),
                            topLeft = Offset(0f, wallY),
                            size = Size(w, h - wallY)
                        )

                        // Sandstone massive fort watch towers left/right
                        drawRect(Color(0xFF42221B), topLeft = Offset(w * 0.08f, wallY - 150f), size = Size(90f, 150f))
                        drawRect(Color(0xFF28130E), topLeft = Offset(w * 0.08f - 5f, wallY - 165f), size = Size(100f, 15f))
                        // Dome watchtower
                        drawArc(
                            color = Color(0xFFE2B08E).copy(alpha = 0.5f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(w * 0.08f + 15f, wallY - 215f),
                            size = Size(60f, 100f)
                        )

                        // Sandstone massive fort watch tower right
                        drawRect(Color(0xFF42221B), topLeft = Offset(w * 0.8f, wallY - 130f), size = Size(80f, 130f))
                        drawRect(Color(0xFF28130E), topLeft = Offset(w * 0.8f - 5f, wallY - 145f), size = Size(90f, 15f))

                        // Mughal brick grid slits
                        for (i in 0..12) {
                            drawArc(
                                color = Color.Black.copy(alpha = 0.3f),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(w * 0.2f + i * 50f, wallY + 25f),
                                size = Size(25f, 40f)
                            )
                        }
                    }
                    PakistanMap.ISLAMABAD -> {
                        // Drawing Pine forests and high altitudes Margalla mountain ranges
                        val groundY = h * 0.6f
                        drawRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF142416), Color(0xFF0A120B))),
                            topLeft = Offset(0f, groundY),
                            size = Size(w, h - groundY)
                        )

                        // Margalla silhouette mountains
                        val mountPath = Path().apply {
                            moveTo(0f, groundY)
                            lineTo(w * 0.25f, groundY - 180f)
                            lineTo(w * 0.5f, groundY - 110f)
                            lineTo(w * 0.8f, groundY - 220f)
                            lineTo(w, groundY)
                            close()
                        }
                        drawPath(
                            path = mountPath,
                            brush = Brush.verticalGradient(listOf(Color(0xFF1E3524), Color(0xFF0F1B12)))
                        )

                        // Draw pine triangular tree shapes
                        val pineRand = Random(441)
                        for (i in 0..10) {
                            val treeX = pineRand.nextFloat() * w
                            val treeBaseY = groundY + pineRand.nextFloat() * 80f
                            val treeH = 90f + pineRand.nextFloat() * 60f
                            val treeW = 35f + pineRand.nextFloat() * 20f

                            val treePath = Path().apply {
                                moveTo(treeX, treeBaseY - treeH)
                                lineTo(treeX - treeW/2, treeBaseY)
                                lineTo(treeX + treeW/2, treeBaseY)
                                close()
                            }
                            drawPath(treePath, Color(0xFF0F2213).copy(alpha = 0.8f))
                        }
                    }
                }

                // DRAW DESTRUCTIBLE PHYSICAL WORLD CONCRETE TARGETS
                scenicTargets.forEach { target ->
                    if (!target.isDestroyed) {
                        val centerX = target.x + target.size / 2
                        val centerY = target.y + target.size / 2

                        // 1. Draw mechanical training frame holding structure
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(centerX, centerY),
                            end = Offset(centerX, centerY + 180f),
                            strokeWidth = 3f
                        )

                        // 2. Draw circular target bodies with outer white border and core red bulls-eyes
                        drawCircle(
                            color = Color.LightGray,
                            radius = target.size / 2,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = target.size * 0.35f,
                            center = Offset(centerX, centerY)
                        )
                        // Bullseye (Vital core core)
                        drawCircle(
                            color = DeadShotTheme.TacticalRed,
                            radius = target.size * 0.16f,
                            center = Offset(centerX, centerY)
                        )

                        // Draw Target hitpoint cracking patterns dynamically matching residual hp percentage!
                        if (target.hp < target.maxHp) {
                            val crackProgress = 1f - (target.hp.toFloat() / target.maxHp.toFloat())
                            val crackSeed = Random(target.lastCrackSeed.toLong())
                            val rayCount = (crackProgress * 12).toInt().coerceAtLeast(3)
                            for (r in 0 until rayCount) {
                                val angle = crackSeed.nextFloat() * 2 * PI.toFloat()
                                val len = (target.size / 2) * (0.3f + crackSeed.nextFloat() * 0.6f)
                                drawLine(
                                    color = Color(0xFF2D2F33),
                                    start = Offset(centerX, centerY),
                                    end = Offset(centerX + cos(angle) * len, centerY + sin(angle) * len),
                                    strokeWidth = 2.5f
                                )
                            }
                        }

                        // Hp tiny gauge text under targets
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(target.x, target.y - 12f),
                            size = Size(target.size, 4f)
                        )
                        val hpPct = target.hp.toFloat() / target.maxHp.toFloat()
                        drawRect(
                            color = if (hpPct > 0.5f) DeadShotTheme.LaserGreen else DeadShotTheme.TacticalRed,
                            topLeft = Offset(target.x, target.y - 12f),
                            size = Size(target.size * hpPct, 4f)
                        )
                    } else {
                        // Drawing shattered broken target crumbling physical rubble flying outward using particle frames
                        val crackSeed = Random(target.id.toLong() * 3)
                        for (r in 0..7) {
                            val fX = target.x + (target.size / 2) + crackSeed.nextFloat() * 40f - 20f
                            val fY = target.y + (target.size / 2) + crackSeed.nextFloat() * 60f + 20f
                            drawCircle(
                                color = Color.Gray,
                                radius = crackSeed.nextFloat() * 6f + 2f,
                                center = Offset(fX, fY)
                            )
                        }
                    }
                }

                // DRONE VIEW CAMERA OVERLAYS (Tactical matrix grid scan lines)
                if (activeCameraAngle == CameraAngle.DRONE) {
                    // Draw horizontal and vertical green digital scanning scanning boundaries and HUD
                    val pulse = (sin(simulationFrame.value * 0.1f) + 1f) / 2f
                    drawRect(
                        color = DeadShotTheme.LaserGreen.copy(alpha = 0.05f)
                    )
                    // High grid lines
                    val lineCount = 10
                    for (i in 0..lineCount) {
                        val rowY = (i * h / lineCount)
                        drawLine(
                            color = DeadShotTheme.LaserGreen.copy(alpha = 0.08f),
                            start = Offset(0f, rowY),
                            end = Offset(w, rowY),
                            strokeWidth = 1f
                        )
                    }
                    // Scan sweeping green bar
                    val sweepY = (simulationFrame.value * 4f) % h
                    drawLine(
                        color = DeadShotTheme.LaserGreen.copy(alpha = 0.25f),
                        start = Offset(0f, sweepY),
                        end = Offset(w, sweepY),
                        strokeWidth = 2f
                    )
                }

                // WEATHER SHADER EMITTER PARTICLES (RAIN, SANDSTORMS)
                when (selectedWeather) {
                    WeatherType.SUNNY -> {
                        // Radiant moving sun glow flare (top-right corner)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                                center = Offset(w * 0.85f, h * 0.15f),
                                radius = 180f
                            ),
                            radius = 180f,
                            center = Offset(w * 0.85f, h * 0.15f)
                        )
                    }
                    WeatherType.MONSOON -> {
                        // Rapid storm rainfall particles flying diagonal-downwards
                        val monsoonRand = Random(771)
                        for (i in 0..60) {
                            val rainX = (monsoonRand.nextFloat() * w + simulationFrame.value * -6f) % w
                            val rainY = (monsoonRand.nextFloat() * h + simulationFrame.value * 12f) % h
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(rainX, rainY),
                                end = Offset(rainX - 8f, rainY + 22f),
                                strokeWidth = 1f
                            )
                        }

                        // Realistic Monsoonal thunderstorm lightning sheets
                        if (simulationFrame.value % 110 == 12 || simulationFrame.value % 110 == 14) {
                            drawRect(color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                    WeatherType.SANDSTORM -> {
                        // Heavy dense horizontal blowing dust storm haze particles
                        val sandRand = Random(905)
                        drawRect(color = Color(0xFFD2B48C).copy(alpha = 0.28f)) // orange dust overlay

                        for (i in 0..40) {
                            val dustSize = 10f + sandRand.nextFloat() * 40f
                            val dustX = (sandRand.nextFloat() * w + simulationFrame.value * -15f) % w
                            val dustY = sandRand.nextFloat() * h
                            drawCircle(
                                color = Color(0xFFCD853F).copy(alpha = 0.12f),
                                radius = dustSize,
                                center = Offset(dustX, dustY)
                            )
                        }
                    }
                }
            }
        }

        // SNIPER SCOPE VIEW OVERLAYS
        // When scoping on AWP, mask the screen inside standard focus optic ring
        if (isScoped && selectedWeapon == WeaponType.AWP) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val scopeRadius = h * 0.44f
                    val cx = w / 2
                    val cy = h / 2

                    // Mask surrounding space outside circular crosshair
                    clipRect {
                        val path = Path().apply {
                            addOval(Rect(cx - scopeRadius, cy - scopeRadius, cx + scopeRadius, cy + scopeRadius))
                        }
                        // Draw black outer mask
                        clipPath(path, clipOp = ClipOp.Difference) {
                            drawRect(Color.Black.copy(alpha = 0.94f))
                        }
                    }

                    // Tactical Crosshair Lines inside optical tube
                    drawLine(Color(0xFF39FF14), Offset(cx - scopeRadius, cy), Offset(cx + scopeRadius, cy), strokeWidth = 1.5f)
                    drawLine(Color(0xFF39FF14), Offset(cx, cy - scopeRadius), Offset(cx, cy + scopeRadius), strokeWidth = 1.5f)

                    // Draw focus rings
                    drawCircle(Color(0xFF39FF14).copy(alpha = 0.4f), radius = scopeRadius, center = Offset(cx, cy), style = Stroke(2f))
                    drawCircle(Color(0xFF39FF14).copy(alpha = 0.2f), radius = scopeRadius * 0.5f, center = Offset(cx, cy), style = Stroke(0.5f))

                    // Tactical windage tick marks
                    for (i in 1..8) {
                        val tickOffsetX = i * (scopeRadius / 10f)
                        drawLine(Color(0xFF39FF14), Offset(cx - tickOffsetX, cy - 3f), Offset(cx - tickOffsetX, cy + 3f), strokeWidth = 1f)
                        drawLine(Color(0xFF39FF14), Offset(cx + tickOffsetX, cy - 3f), Offset(cx + tickOffsetX, cy + 3f), strokeWidth = 1f)
                    }

                    // Scope red dot core center
                    drawCircle(Color.Red, radius = 2.5f, center = Offset(cx, cy))
                }
            }
        }

        // 3D/VECTOR FIRST-PERSON WEAPON HUD COMPOSABLE
        // Positioned at the target of first-person view
        if (activeCameraAngle == CameraAngle.FIRST_PERSON && (!isScoped || selectedWeapon != WeaponType.AWP)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(420.dp)
                    .height(260.dp)
                    .offset(y = recoilOffset.dp) // Weapon mechanical recoil offset jump
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Center-back gun barrel coordinates
                    val originX = w * 0.4f
                    val originY = h * 0.9f

                    when (selectedWeapon) {
                        WeaponType.DEAGLE -> {
                            // Heavy chrome silver pistol construction
                            val bodyBrush = Brush.linearGradient(listOf(Color(0xFFC0C0C0), Color(0xFF4A4A4A)))
                            val slidePath = Path().apply {
                                moveTo(originX, originY)
                                lineTo(originX - 120f, originY - 110f)
                                lineTo(originX + 180f, originY - 110f)
                                lineTo(originX + 220f, originY)
                                close()
                            }
                            drawPath(slidePath, bodyBrush)
                            // Slide chamber details
                            drawRect(Color.DarkGray, topLeft = Offset(originX, originY - 100f), size = Size(80f, 18f))

                            // Barrel hole
                            drawCircle(Color.Black, radius = 10f, center = Offset(originX - 100f, originY - 100f))

                            // Heavy grip
                            val gripPath = Path().apply {
                                moveTo(originX + 100f, originY - 40f)
                                lineTo(originX + 240f, originY + 120f)
                                lineTo(originX + 160f, originY + 120f)
                                lineTo(originX + 60f, originY - 40f)
                                close()
                            }
                            drawPath(gripPath, Color(0xFF1C1D21))
                        }
                        WeaponType.M4A1 -> {
                            // Long matte-black assault rifle, tactical suppressor cylinder
                            val carbineBrush = Brush.linearGradient(listOf(Color(0xFF1E2129), Color(0xFF030305)))

                            // Suppressor tube
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(originX - 180f, originY - 75f),
                                size = Size(140f, 22f),
                                cornerRadius = CornerRadius(4f)
                            )

                            // Foregrips & rails
                            drawRect(
                                brush = carbineBrush,
                                topLeft = Offset(originX - 40f, originY - 82f),
                                size = Size(200f, 40f)
                            )
                            // Magazine well receiver
                            drawRect(
                                color = Color(0xFF242730),
                                topLeft = Offset(originX + 100f, originY - 50f),
                                size = Size(100f, 70f)
                            )
                            // Top mounted holograph reflex scope optics
                            drawRect(Color.DarkGray, topLeft = Offset(originX + 40f, originY - 110f), size = Size(50f, 28f))
                            drawArc(Color(0xFF39FF14).copy(alpha = 0.5f), 0f, 360f, true, topLeft = Offset(originX + 50f, originY - 105f), size = Size(10f, 10f))
                        }
                        WeaponType.AWP -> {
                            // Massive green polymer bolt rifle with large glass scope optics tube on top
                            val frameColor = Color(0xFF2E4F2B) // Olive drab sniper green

                            // Extra long solid steel alloy barrel extending out of frame
                            drawRect(Color.DarkGray, topLeft = Offset(originX - 250f, originY - 52f), size = Size(260f, 10f))

                            // Large bolt action housing stock
                            val sniperStockPath = Path().apply {
                                moveTo(originX - 30f, originY - 62f)
                                lineTo(originX + 220f, originY - 62f)
                                lineTo(originX + 260f, originY + 120f)
                                lineTo(originX + 110f, originY + 120f)
                                close()
                            }
                            drawPath(sniperStockPath, frameColor)

                            // Giant scope cylinder tube on top
                            drawRoundRect(
                                color = Color(0xFF111215),
                                topLeft = Offset(originX + 20f, originY - 110f),
                                size = Size(130f, 32f),
                                cornerRadius = CornerRadius(6f)
                            )
                            // Bolt lever handle detail
                            drawLine(Color.LightGray, Offset(originX + 160f, originY - 55f), Offset(originX + 180f, originY - 85f), strokeWidth = 5f)
                            drawCircle(Color.LightGray, radius = 6f, center = Offset(originX + 180f, originY - 85f))
                        }
                    }

                    // MUZZLE COMPRESSION BLAST EXHAUST FLASHLIGHT ON ACTIVE SHOOTING
                    if (isFiring) {
                        val blastPulse = Random.nextFloat() * 25f + 35f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFFFFFB3), Color(0xFFFFCC00).copy(alpha = 0.6f), Color.Transparent),
                                center = Offset(originX - 140f, originY - 80f),
                                radius = blastPulse
                            ),
                            radius = blastPulse,
                            center = Offset(originX - 140f, originY - 80f)
                        )
                    }
                }
            }
        }

        // FLOATING AIMING RETICLE OVERLAY
        // Renders when in normal eye level modes
        if (!isScoped || selectedWeapon != WeaponType.AWP) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.85f)
            ) {
                // Crosshair HUD lines showing weapon recoil expansion sway dynamically
                val crossSize = if (isFiring) 22f else 14f
                val strokeThickness = 2f
                val markerColor = when (selectedWeapon) {
                    WeaponType.AWP -> DeadShotTheme.NeonBlue
                    else -> DeadShotTheme.LaserGreen
                }

                // Dynamic reticle outer circles
                drawCircle(markerColor.copy(alpha = 0.3f), radius = crossSize, center = Offset(crosshairX, crosshairY), style = Stroke(1f))

                // Four segment crosshairs
                drawLine(markerColor, Offset(crosshairX - crossSize, crosshairY), Offset(crosshairX - 4f, crosshairY), strokeWidth = strokeThickness)
                drawLine(markerColor, Offset(crosshairX + 4f, crosshairY), Offset(crosshairX + crossSize, crosshairY), strokeWidth = strokeThickness)
                drawLine(markerColor, Offset(crosshairX, crosshairY - crossSize), Offset(crosshairX, crosshairY - 4f), strokeWidth = strokeThickness)
                drawLine(markerColor, Offset(crosshairX, crosshairY + 4f), Offset(crosshairX, crosshairY + crossSize), strokeWidth = strokeThickness)

                // Precise tiny center dot
                drawCircle(Color.Red, radius = 1.5f, center = Offset(crosshairX, crosshairY))
            }
        }

        // ON-SCREEN COMPACT NAVIGATION & COMBAT SCOREBOARD OVERLAY
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // HUD HEADER ROW (TOP BAR PARAMETERS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Back button to lobby
                    FilledIconButton(
                        onClick = { viewModel.navigateTo(ActiveScreen.LOBBY) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.7f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_back_to_lobby")
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = "MAP: ${selectedMap.label.uppercase()}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DeadShotTheme.LaserGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "WEATHER: ${selectedWeather.label.uppercase()}",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // CENTRAL FEEDBACK DIALOG
                Box(
                    modifier = Modifier
                        .alpha(if (combatFeedbackMessage.isNotEmpty()) 1.0f else 0f)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .border(1.5.dp, DeadShotTheme.TacticalRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = combatFeedbackMessage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                // RIGHT HUD: CORE TARGET SCORE
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, DeadShotTheme.GoldAccent.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "TRAINING SCORE",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                        Text(
                            text = "${"%,04d".format(score)} PTS",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = DeadShotTheme.GoldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // COMBAT IN-MATCH CONTROL ACTIONS (COMBAT SLIDER FOOTER RADIAL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT FLANK: Camera Angle & Optical Scope toggler controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // CAMERA ANGLE CYCLE TOGGLER
                    Button(
                        onClick = { viewModel.toggleCameraAngle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(38.dp).testTag("btn_camera_angle")
                    ) {
                        Text(
                            text = "[ CAM: ${activeCameraAngle.label.uppercase()} ]",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }

                    // AWP SCOPE OPTIC TOGGLER
                    if (selectedWeapon == WeaponType.AWP) {
                        Button(
                            onClick = { viewModel.toggleScope() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScoped) DeadShotTheme.NeonBlue else Color.Black.copy(alpha = 0.8f)
                            ),
                            border = BorderStroke(1.dp, if (isScoped) DeadShotTheme.NeonBlue else Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(38.dp).testTag("btn_awp_scope")
                        ) {
                            Text(
                                text = if (isScoped) "OPTICS: SCOPED" else "OPTICS: UNSCOPED",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isScoped) Color.Black else Color.White
                            )
                        }
                    }
                }

                // CENTER FLANK: Ammo and reload actions widget
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .width(180.dp)
                        .padding(bottom = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedWeapon.label.substringBefore(" "),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.LightGray
                            )
                            Text(
                                text = if (isReloading) "SWAPPING..." else "$currentAmmo / ${selectedWeapon.ammoMax}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAmmo <= (selectedWeapon.ammoMax * 0.3)) DeadShotTheme.TacticalRed else DeadShotTheme.LaserGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Visual bullet icons grid
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (b in 1..selectedWeapon.ammoMax) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .background(
                                            if (b <= currentAmmo) DeadShotTheme.LaserGreen 
                                            else Color.DarkGray.copy(alpha = 0.4f)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { viewModel.reloadWeapon() },
                            modifier = Modifier.fillMaxWidth().height(24.dp).testTag("btn_reload"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("TAP TO RE-MAGAZINE", fontSize = 9.sp, color = DeadShotTheme.NeonBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // RIGHT FLANK: THE GIANT RED FIRING TRIGGER BUTTON!
                Button(
                    onClick = {
                        // Fire bullet centered precisely at current crosshair location coordinates!
                        viewModel.fireWeapon(crosshairX, crosshairY) { statusMsg ->
                            combatFeedbackMessage = statusMsg
                            feedbackTriggerToggle = !feedbackTriggerToggle
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 12.dp, spotColor = DeadShotTheme.TacticalRed, ambientColor = DeadShotTheme.TacticalRed)
                        .testTag("btn_fire_gun")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "FIRE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Reset hit indicators delay popup feedback
    LaunchedEffect(feedbackTriggerToggle) {
        if (combatFeedbackMessage.isNotEmpty()) {
            delay(1200)
            combatFeedbackMessage = ""
        }
    }
}
