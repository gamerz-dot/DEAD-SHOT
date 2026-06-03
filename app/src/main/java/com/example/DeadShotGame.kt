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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
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
    val isParachuteGlidePhase by viewModel.isParachuteGlidePhase.collectAsState()
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
            if (isParachuteGlidePhase) {
                DeadShotParachuteScreen(viewModel = viewModel)
            } else {
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
}

@Composable
fun DeadShotParachuteScreen(viewModel: DeadShotViewModel) {
    val dropHeight by viewModel.parachuteDropHeight.collectAsState()
    val isParachuteOpened by viewModel.isParachuteOpened.collectAsState()
    val selectedMap by viewModel.selectedMap.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val currentOutfit by viewModel.equippedOutfit.collectAsState()

    val infiniteTransition = rememberInfiniteTransition()
    
    val windOscillation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.0f at 0
                0.0f at 2400
                0.8f at 2450
                0.0f at 2500
                1.0f at 2550
                0.0f at 2650
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080A))
    ) {
        // SCENIC LANDSCAPE CANVAS DRAWINGS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw weather sky backgrounds
            when (selectedWeather) {
                WeatherType.SUNNY -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(w * 0.8f, h * 0.2f),
                            radius = 180f
                        ),
                        radius = 180f,
                        center = Offset(w * 0.8f, h * 0.2f)
                    )
                }
                WeatherType.MONSOON -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1F2533), Color(0xFF0D1017))
                        )
                    )
                    if (lightningAlpha > 0.1f) {
                        val path = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            lineTo(w * 0.45f, h * 0.25f)
                            lineTo(w * 0.52f, h * 0.25f)
                            lineTo(w * 0.48f, h * 0.5f)
                            lineTo(w * 0.54f, h * 0.42f)
                            lineTo(w * 0.5f, h * 0.7f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFE2F0FF).copy(alpha = lightningAlpha),
                            style = Stroke(width = 4f)
                        )
                    }
                }
                WeatherType.SANDSTORM -> {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF9E7854), Color(0xFF422F1D))
                        )
                    )
                }
            }

            // 2. Draw ground surface details (Perspective depth) that expands as altitude decreases
            val scalePct = (200f - dropHeight) / 200f
            val perspectiveOffset = h * 0.6f + (h * 0.2f * scalePct)
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1C1D24), Color(0xFF0A0B0F))
                ),
                topLeft = Offset(0f, perspectiveOffset),
                size = Size(w, h - perspectiveOffset)
            )

            // Draw Pakistani thematic structures
            when (selectedMap) {
                PakistanMap.KARACHI -> {
                    drawRect(
                        color = Color(0xFF14243B),
                        topLeft = Offset(0f, perspectiveOffset + 15f),
                        size = Size(w * 0.4f, h)
                    )
                    val containerSize = 10f + (35f * scalePct)
                    for (i in 0..12) {
                        val cX = w * 0.2f + (i * w * 0.05f)
                        val cY = perspectiveOffset + 5f + (i * 4f)
                        drawRect(
                            color = if (i % 2 == 0) Color(0xFFC0392B) else Color(0xFF2980B9),
                            topLeft = Offset(cX, cY),
                            size = Size(containerSize * 1.5f, containerSize)
                        )
                    }
                    val flagX = w * 0.85f
                    val flagY = perspectiveOffset - 25f
                    drawRect(color = Color(0xFF01411C), topLeft = Offset(flagX, flagY), size = Size(24f, 15f))
                    drawRect(color = Color.White, topLeft = Offset(flagX, flagY), size = Size(6f, 15f))
                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(flagX + 15f, flagY + 7.5f))
                }
                PakistanMap.LAHORE -> {
                    val fortressW = w * 0.6f
                    val fortressH = 15f + (80f * scalePct)
                    val fortressX = w * 0.2f
                    drawRoundRect(
                        color = Color(0xFF8E44AD),
                        topLeft = Offset(fortressX, perspectiveOffset - fortressH + 5f),
                        size = Size(fortressW, fortressH),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    val domeR = 12f + (35f * scalePct)
                    drawCircle(
                        color = Color(0xFFD4AC0D),
                        radius = domeR,
                        center = Offset(w * 0.4f, perspectiveOffset - fortressH)
                    )
                    drawCircle(
                        color = Color(0xFFD4AC0D),
                        radius = domeR,
                        center = Offset(w * 0.6f, perspectiveOffset - fortressH)
                    )
                }
                PakistanMap.ISLAMABAD -> {
                    val hillRand = Random(777)
                    for (i in 0..6) {
                        val hillX = i * w * 0.16f
                        val hillH = 20f + (100f * scalePct)
                        val path = Path().apply {
                            moveTo(hillX, perspectiveOffset + 10f)
                            lineTo(hillX + 50f, perspectiveOffset - hillH)
                            lineTo(hillX + 100f, perspectiveOffset + 10f)
                            close()
                        }
                        drawPath(path, color = Color(0xFF1E824C))
                    }
                }
            }

            // 3. Render 3D Flight Drop Jet (altitude > 50m)
            if (dropHeight > 50f) {
                val planeProgress = (200f - dropHeight) / 150f
                val planeX = -100f + (w + 200f) * planeProgress
                val planeY = h * 0.25f + windOscillation

                val planePath = Path().apply {
                    moveTo(planeX, planeY)
                    lineTo(planeX - 60f, planeY - 20f)
                    lineTo(planeX - 90f, planeY - 15f)
                    lineTo(planeX - 70f, planeY)
                    lineTo(planeX - 120f, planeY + 5f)
                    lineTo(planeX - 40f, planeY + 12f)
                    close()
                }
                drawPath(planePath, brush = Brush.linearGradient(colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F))))
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFF00), Color(0xFFFF5722), Color.Transparent),
                        center = Offset(planeX - 120f, planeY + 5f),
                        radius = 20f
                    ),
                    radius = 20f,
                    center = Offset(planeX - 120f, planeY + 5f)
                )
            } else {
                // 4. Parachute Glider deploy (< 50m)
                val canopyX = w * 0.5f + windOscillation
                val canopyY = h * 0.18f
                val canopyW = 100f + (60f * scalePct)
                val canopyH = 30f + (15f * scalePct)

                val pilotX = w * 0.5f + windOscillation
                val pilotY = h * 0.55f

                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(canopyX - canopyW/2, canopyY + canopyH/2), end = Offset(pilotX, pilotY), strokeWidth = 1.5f)
                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(canopyX - canopyW/4, canopyY + canopyH), end = Offset(pilotX, pilotY), strokeWidth = 1.5f)
                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(canopyX + canopyW/4, canopyY + canopyH), end = Offset(pilotX, pilotY), strokeWidth = 1.5f)
                drawLine(color = Color.White.copy(alpha = 0.5f), start = Offset(canopyX + canopyW/2, canopyY + canopyH/2), end = Offset(pilotX, pilotY), strokeWidth = 1.5f)

                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Red, Color.Black, Color.Red, Color.Black)
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(canopyX - canopyW / 2, canopyY),
                    size = Size(canopyW, canopyH * 2)
                )

                val pilotColor = if (currentOutfit == "Angel Suit Outfit") Color(0xFFFFD700) else Color(0xFF39FF14)
                drawCircle(color = pilotColor, radius = 9f, center = Offset(pilotX, pilotY))
                
                drawLine(color = pilotColor, start = Offset(pilotX, pilotY + 9f), end = Offset(pilotX, pilotY + 30f), strokeWidth = 3f)
                
                if (currentOutfit == "Angel Suit Outfit") {
                    val wingPath = Path().apply {
                        moveTo(pilotX, pilotY + 12f)
                        lineTo(pilotX - 35f, pilotY - 5f)
                        lineTo(pilotX - 10f, pilotY + 20f)
                        close()
                        moveTo(pilotX, pilotY + 12f)
                        lineTo(pilotX + 35f, pilotY - 5f)
                        lineTo(pilotX + 10f, pilotY + 20f)
                        close()
                    }
                    drawPath(wingPath, brush = Brush.linearGradient(colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD700))))
                }
            }

            val windRand = Random(555)
            for (i in 0..10) {
                val windX = windRand.nextFloat() * w
                val windStartY = windRand.nextFloat() * h
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(windX, windStartY),
                    end = Offset(windX, (windStartY - 60f).coerceAtLeast(0f)),
                    strokeWidth = 2f
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAKISTAN TACTICAL DROP: ${selectedMap.label.uppercase()}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ALTITUDE: ",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${"%.0f".format(dropHeight)}M",
                        color = if (dropHeight <= 50f) DeadShotTheme.TacticalRed else DeadShotTheme.LaserGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(dropHeight / 200f)
                            .background(
                                color = if (dropHeight <= 50f) DeadShotTheme.TacticalRed else DeadShotTheme.LaserGreen,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("TACTICAL TELEMETRY", color = DeadShotTheme.LaserGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                Text("MAP COORDS: Saddar Port Docks 25°N-67°E", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("EST. WINDSPEED: 42 KNOTS SE", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("EQUIPPED SUIT: $currentOutfit", color = DeadShotTheme.GoldAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (dropHeight > 50f) "FLIGHT DROPPING..." else "PARACHUTE DECELE GLIDING!",
                    color = if (dropHeight > 50f) Color.Yellow else DeadShotTheme.LaserGreen,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    viewModel.skipParachuteGlide {
                        viewModel.setupArenaTargets()
                        viewModel.navigateTo(ActiveScreen.ARENA)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(42.dp)
                    .shadow(elevation = 12.dp, spotColor = DeadShotTheme.TacticalRed, ambientColor = DeadShotTheme.TacticalRed)
                    .testTag("btn_skip_glider")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FORCE JUMP LANDING", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("[SKIP]", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace)
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.submitProfileName(textInput.text)
                        }),
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
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.submitProfileName(textInput.text)
                    },
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

    // NEW STATES FROM VIEWMODEL
    val selectedGameMode by viewModel.selectedGameMode.collectAsState()
    val equippedOutfit by viewModel.equippedOutfit.collectAsState()
    val unlockedOutfits by viewModel.unlockedOutfits.collectAsState()
    val isDailyLimitAngelBundlePurchased by viewModel.isDailyLimitAngelBundlePurchased.collectAsState()
    val dsgCurrency by viewModel.dsgCurrency.collectAsState()
    val equippedCharacter by viewModel.equippedCharacter.collectAsState()
    val equippedActiveSkill by viewModel.equippedActiveSkill.collectAsState()
    val equippedPassiveSkill by viewModel.equippedPassiveSkill.collectAsState()
    val unlockedCharacters by viewModel.unlockedCharacters.collectAsState()
    val equippedWeaponSkinsVal by viewModel.equippedWeaponSkins.collectAsState()
    val unlockedWeaponSkinsVal by viewModel.unlockedWeaponSkins.collectAsState()
    
    val sensitivityVal by viewModel.sensitivityVal.collectAsState()
    val redDotSizeVal by viewModel.redDotSizeVal.collectAsState()
    val joystickScaleVal by viewModel.joystickScaleVal.collectAsState()
    val fireButtonScaleVal by viewModel.fireButtonScaleVal.collectAsState()
    val retroSoundOn by viewModel.retroSoundOn.collectAsState()

    // Dialog toggles
    var showStoreDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showVaultDialog by remember { mutableStateOf(false) }
    var showMapSelectionDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // FREE FIRE LUCK ROYALE LOCAL STATES
    var storeActiveTab by remember { mutableStateOf("ROYALE") } // "ROYALE", "CHARACTERS", "BUNDLES"
    var lastPrizeDrawn by remember { mutableStateOf("") }
    var isSpinningRoyale by remember { mutableStateOf(false) }
    var expandedActiveSkillSelector by remember { mutableStateOf(false) }
    var expandedPassiveSkillSelector by remember { mutableStateOf(false) }

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

                // CURRENCY PANEL (DSG currency tracker)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xD9101217), RoundedCornerShape(8.dp))
                        .border(1.5.dp, DeadShotTheme.GoldAccent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("dsg_currency_panel")
                ) {
                    val gemTransition = rememberInfiniteTransition()
                    val dsgScale by gemTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    // Stylized Golden Shield Diamond / Coin Vector
                    Canvas(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(dsgScale)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width * 0.5f, 0f)
                            lineTo(size.width, size.height * 0.35f)
                            lineTo(size.width * 0.8f, size.height)
                            lineTo(size.width * 0.2f, size.height)
                            lineTo(0f, size.height * 0.35f)
                            close()
                        }
                        drawPath(path, brush = Brush.verticalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${"%,d".format(dsgCurrency)} DSG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DeadShotTheme.GoldAccent,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "GOLD GEMS",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // HIGH CONFLICT SETTINGS COG WHEEL TRIGGER
                Box(
                    modifier = Modifier
                        .background(Color(0xD9101217), RoundedCornerShape(8.dp))
                        .border(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .size(38.dp)
                        .clickable { showSettingsDialog = true }
                        .testTag("btn_lobby_settings"),
                    contentAlignment = Alignment.Center
                ) {
                    val settingsTransit = rememberInfiniteTransition()
                    val rotAngle by settingsTransit.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Canvas(
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotAngle)
                    ) {
                        val strokeW = 4f
                        val gearRadius = size.width * 0.35f
                        val ctr = center

                        drawCircle(
                            color = DeadShotTheme.LaserGreen,
                            radius = gearRadius,
                            center = ctr,
                            style = Stroke(width = strokeW)
                        )
                        drawCircle(
                            color = DeadShotTheme.LaserGreen,
                            radius = gearRadius / 2f,
                            center = ctr
                        )

                        for (i in 0 until 8) {
                            val angleRad = (i * 45) * PI / 180f
                            val startX = ctr.x + cos(angleRad).toFloat() * gearRadius
                            val startY = ctr.y + sin(angleRad).toFloat() * gearRadius
                            val endX = ctr.x + cos(angleRad).toFloat() * (gearRadius + 5f)
                            val endY = ctr.y + sin(angleRad).toFloat() * (gearRadius + 5f)

                            drawLine(
                                color = DeadShotTheme.LaserGreen,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 4f,
                                cap = StrokeCap.Round
                            )
                        }
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
                    // CONFIG CARDS ROWS (WEAPONS, WEATHERS) - MAP SELECTION MOVED TO BOTTOM DOCK
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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

                        // 4. THE GAME MODES DECK (SOLO / DUO / SQUAD) WITH ACTIVE COMBATANT BREAKDOWNS
                        Column {
                            Text(
                                text = "TEAM SQUAD DEPLOYMENT MODE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("SOLO", "DUO", "SQUAD").forEach { mode ->
                                    val isSel = selectedGameMode == mode
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSel) DeadShotTheme.CarbonBlack else Color(0x6616181C)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSel) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.05f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clickable { viewModel.selectGameMode(mode) }
                                            .testTag("mode_card_$mode"),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSel) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.6f),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(DeadShotTheme.LaserGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WARZONE: ${selectedMap.locationName.uppercase()} CLIMATE: ${selectedWeather.label.uppercase()} FIREARM: ${selectedWeapon.label.uppercase()}",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = DeadShotTheme.MetallicSilver.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            val totalCombatants = when(selectedMap) {
                                PakistanMap.KARACHI -> 50
                                PakistanMap.LAHORE -> 50
                                PakistanMap.ISLAMABAD -> 100
                            }
                            val diffLabel = when(selectedMap) {
                                PakistanMap.KARACHI -> "EASY"
                                PakistanMap.LAHORE -> "SO BUT HARD-MODE"
                                PakistanMap.ISLAMABAD -> "HARD-MODE"
                            }
                            val teamQuantity = when(selectedGameMode) {
                                "SOLO" -> 1
                                "DUO" -> 2
                                "SQUAD" -> 4
                                else -> 1
                            }
                            val npcEnemies = totalCombatants - teamQuantity

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(DeadShotTheme.TacticalRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "DISPATCH BRIEF: $selectedGameMode ($diffLabel) - TOTAL COMBATANTS: $totalCombatants [YOU: 1, TEAM: ${teamQuantity - 1}, ENEMY TARGETS: $npcEnemies]",
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = DeadShotTheme.LaserGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                                                if (customChatInput.text.isNotBlank()) {
                                                    viewModel.sendChatMessage(customChatInput.text)
                                                    customChatInput = TextFieldValue("")
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
                                                }
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
                                                    if (customChatInput.text.isNotBlank()) {
                                                        viewModel.sendChatMessage(customChatInput.text)
                                                        customChatInput = TextFieldValue("")
                                                        keyboardController?.hide()
                                                        focusManager.clearFocus()
                                                    }
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

            // BOTTOM BAR: START GAME LAUNCH BINDER PANEL (FREE FIRE 4K ACCENT CLUSTER)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Left-aligned tactical control keys
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(ActiveScreen.LOGIN) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("LOGOUT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    // DSA ACHIEVEMENT CABINET BUTTON
                    OutlinedButton(
                        onClick = { showAchievementsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeadShotTheme.LaserGreen),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.height(46.dp).testTag("btn_lobby_dsa")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("🏆 DSA", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("AWARDS", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // VAULT CLOTHING COLLECTIONS CABINET BUTTON
                    OutlinedButton(
                        onClick = { showVaultDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeadShotTheme.LaserGreen),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.height(46.dp).testTag("btn_lobby_vault")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("👕 VAULT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("SUITS", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // NPC GIFT STORE BUTTON
                    OutlinedButton(
                        onClick = { showStoreDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4D4D)),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.25f)),
                        modifier = Modifier.height(46.dp).testTag("btn_lobby_store")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("🎁 GIFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("ANGEL", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Right portion: Stack containing MAP SELECTION WIDGET above big DEPLOY Button!
                Column(
                    modifier = Modifier.width(310.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // NEW 4K STYLISH MAP SELECTION INTERACTIVE WIDGET (Free Fire style)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { showMapSelectionDialog = true }
                            .testTag("btn_lobby_map_select_widget"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14161C)),
                        border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // High fidelity background canvas drawing representing the selected map inside a radar/grid!
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                
                                // Draw grid background
                                for (x in 0..10) {
                                    val xx = x * (width / 10f)
                                    drawLine(
                                        color = Color(0x1539FF14),
                                        start = Offset(xx, 0f),
                                        end = Offset(xx, height),
                                        strokeWidth = 1f
                                    )
                                }
                                for (y in 0..5) {
                                    val yy = y * (height / 5f)
                                    drawLine(
                                        color = Color(0x1539FF14),
                                        start = Offset(0f, yy),
                                        end = Offset(width, yy),
                                        strokeWidth = 1f
                                    )
                                }

                                // Draw specific abstract indicators for each map to look ultra high tech
                                when (selectedMap) {
                                    PakistanMap.KARACHI -> {
                                        drawCircle(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 28f
                                        )
                                        drawCircle(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 3f
                                        )
                                        drawLine(Color(0xFF00E5FF).copy(alpha = 0.4f), Offset(width*0.15f, height*0.4f), Offset(width*0.25f, height*0.4f), strokeWidth = 2f)
                                        drawLine(Color(0xFF00E5FF).copy(alpha = 0.4f), Offset(width*0.12f, height*0.6f), Offset(width*0.22f, height*0.6f), strokeWidth = 2f)
                                    }
                                    PakistanMap.LAHORE -> {
                                        drawCircle(
                                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 28f
                                        )
                                        drawCircle(
                                            color = Color(0xFFFF5252).copy(alpha = 0.8f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 3f
                                        )
                                        drawLine(Color(0xFFFF5252).copy(alpha = 0.4f), Offset(width*0.12f, height*0.7f), Offset(width*0.12f, height*0.3f), strokeWidth = 3f)
                                        drawLine(Color(0xFFFF5252).copy(alpha = 0.4f), Offset(width*0.12f, height*0.3f), Offset(width*0.2f, height*0.3f), strokeWidth = 3f)
                                    }
                                    PakistanMap.ISLAMABAD -> {
                                        drawCircle(
                                            color = DeadShotTheme.LaserGreen.copy(alpha = 0.15f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 28f
                                        )
                                        drawCircle(
                                            color = DeadShotTheme.LaserGreen.copy(alpha = 0.8f),
                                            center = Offset(width * 0.2f, height * 0.5f),
                                            radius = 3f
                                        )
                                        val path = Path().apply {
                                            moveTo(width * 0.12f, height * 0.7f)
                                            lineTo(width * 0.2f, height * 0.35f)
                                            lineTo(width * 0.28f, height * 0.7f)
                                        }
                                        drawPath(path, color = DeadShotTheme.LaserGreen.copy(alpha = 0.35f), style = Stroke(width = 2f))
                                    }
                                }
                            }

                            // Glassmorphic text and selections info
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0x33000000), RoundedCornerShape(6.dp))
                                        .border(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (selectedMap) {
                                            PakistanMap.KARACHI -> "🚢"
                                            PakistanMap.LAHORE -> "🏰"
                                            PakistanMap.ISLAMABAD -> "⛰️"
                                        },
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "MAP SECTOR // ",
                                            fontSize = 7.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = DeadShotTheme.LaserGreen.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "READY",
                                            fontSize = 6.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF39FF14)
                                        )
                                    }
                                    Text(
                                        text = selectedMap.label.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "MODE: $selectedGameMode | ${selectedWeather.label.uppercase()}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(DeadShotTheme.LaserGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, DeadShotTheme.LaserGreen, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "TAP TO MAP",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeadShotTheme.LaserGreen,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "➔",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeadShotTheme.LaserGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // GIANT Start deployment button
                    Button(
                        onClick = {
                            viewModel.startParachuteGlideSequence {
                                viewModel.setupArenaTargets()
                                viewModel.navigateTo(ActiveScreen.ARENA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeadShotTheme.LaserGreen
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(elevation = 10.dp, spotColor = DeadShotTheme.LaserGreen, ambientColor = DeadShotTheme.LaserGreen)
                            .testTag("btn_launch_sandbox")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "BATTLE ZONE START",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡",
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // 1. SETTINGS OVERLAY
        if (showSettingsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable { showSettingsDialog = false }
                    .testTag("modal_settings"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF16181C),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚙️ SYSTEM TACTICAL SETTINGS PANEL", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Button(
                                onClick = { showSettingsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("CLOSE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Text("AIM SENSITIVITY MULTIPLIER", color = DeadShotTheme.LaserGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("Current: ${"%.1f".format(sensitivityVal)}x", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = sensitivityVal,
                                    onValueChange = { viewModel.setSensitivity(it) },
                                    valueRange = 0.1f..3.0f,
                                    colors = SliderDefaults.colors(thumbColor = DeadShotTheme.LaserGreen, activeTrackColor = DeadShotTheme.LaserGreen)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("RED DOT SCOPE CROSSHAIR SIZE", color = DeadShotTheme.LaserGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("Current: ${redDotSizeVal.toInt()}DP", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = redDotSizeVal,
                                    onValueChange = { viewModel.setRedDotSize(it) },
                                    valueRange = 4f..30f,
                                    colors = SliderDefaults.colors(thumbColor = DeadShotTheme.LaserGreen, activeTrackColor = DeadShotTheme.LaserGreen)
                                )
                            }

                            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                Text("HUD JOYSTICK CONTROL SCALE", color = DeadShotTheme.LaserGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("Current: ${"%.1f".format(joystickScaleVal)}x", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = joystickScaleVal,
                                    onValueChange = { viewModel.setJoystickScale(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = DeadShotTheme.LaserGreen, activeTrackColor = DeadShotTheme.LaserGreen)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("HUD FIRE / RELOAD KEY SCALE", color = DeadShotTheme.LaserGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("Current: ${"%.1f".format(fireButtonScaleVal)}x", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = fireButtonScaleVal,
                                    onValueChange = { viewModel.setFireButtonScale(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = DeadShotTheme.LaserGreen, activeTrackColor = DeadShotTheme.LaserGreen)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
                                        .clickable { viewModel.toggleRetroSoundOn() }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("14:11 RETRO SOUND ERA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Toggle 90s vintage soundfx synth", color = Color.White.copy(alpha = 0.5f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Switch(
                                        checked = retroSoundOn,
                                        onCheckedChange = { viewModel.toggleRetroSoundOn() },
                                        colors = SwitchDefaults.colors(checkedThumbColor = DeadShotTheme.LaserGreen, checkedTrackColor = DeadShotTheme.LaserGreen.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. ACHIEVEMENTS OVERLAY
        if (showAchievementsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable { showAchievementsDialog = false }
                    .testTag("modal_achievements"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF16181C),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .fillMaxHeight(0.8f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏅 DSA - DEAD SHOT ACHIEVEMENTS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Button(
                                onClick = { showAchievementsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("CLOSE", fontSize = 9.sp)
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                AchievementItem(title = "SADDAR CODENAME SURVIVOR", desc = "Land successfully at Karachi Docks Map and survey over 21 structures.", unlocked = true)
                            }
                            item {
                                AchievementItem(title = "LAHORE BULLET SHIELD", desc = "Defeat combat targets in Normal/Hard difficulty on Lahore Fort.", unlocked = true)
                            }
                            item {
                                val hasAngelSuit = unlockedOutfits.contains("Angel Suit Outfit")
                                AchievementItem(title = "ANGEL DEPLOYMENT CONQUEROR", desc = "Purchase the limited Angel bundle of 10 Days limit.", unlocked = hasAngelSuit)
                            }
                            item {
                                val holdManyRubies = rubies >= 1000
                                AchievementItem(title = "DSC WAR MILLIONAIRE", desc = "Hold over 1,000 active ruby currency credits.", unlocked = holdManyRubies)
                            }
                        }
                    }
                }
            }
        }

        // 3. VAULT OUTIFTS & SKIN CABINET (4K FREE FIRE REPLICA)
        if (showVaultDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .clickable { showVaultDialog = false }
                    .testTag("modal_vault"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF0F1115),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, DeadShotTheme.GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.82f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥋 VAULT CUSTOMIZATION CABINET", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Button(
                                onClick = { showVaultDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("CLOSE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column: Outfits Cabinet
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text("👕 UNLOCKED SQUAD OUTFITS", color = DeadShotTheme.GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(unlockedOutfits) { outfit ->
                                        val isEquipped = equippedOutfit == outfit
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isEquipped) Color(0xFF231E12) else Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                                .border(1.dp, if (isEquipped) DeadShotTheme.GoldAccent else Color.Transparent, RoundedCornerShape(6.dp))
                                                .clickable { viewModel.equipOutfit(outfit) }
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(outfit.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text(
                                                    text = when(outfit) {
                                                        "Standard Outfit" -> "Standard green combat field camouflage."
                                                        "Tactical Camo" -> "Saddar Navy and desert dunes stealth mesh."
                                                        "Golden Elite Armor" -> "Heavy golden alloys reflecting laser fields."
                                                        "Angel Suit Outfit" -> "Premium feathered bundle with wing thrusters!"
                                                        else -> outfit
                                                    },
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 7.5.sp,
                                                    lineHeight = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Button(
                                                onClick = { viewModel.equipOutfit(outfit) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isEquipped) DeadShotTheme.GoldAccent else Color.White.copy(alpha = 0.08f)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(26.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (isEquipped) "EQUIPPED" else "EQUIP",
                                                    fontSize = 8.sp,
                                                    color = if (isEquipped) Color.Black else Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Right Column: Weapon Skins Cabinet
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text("🔫 UNLOCKED WEAPON COSMETICS", color = DeadShotTheme.GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(unlockedWeaponSkinsVal) { skin ->
                                        val isEquippedAWP = equippedWeaponSkinsVal["AWP"] == skin
                                        val isEquippedM4 = equippedWeaponSkinsVal["M4A1"] == skin
                                        val isEquippedDeagle = equippedWeaponSkinsVal["DEAGLE"] == skin
                                        val isAnyEquipped = isEquippedAWP || isEquippedM4 || isEquippedDeagle

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isAnyEquipped) Color(0xFF1E231C) else Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                                .border(1.dp, if (isAnyEquipped) DeadShotTheme.LaserGreen else Color.Transparent, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(skin.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text(
                                                    text = when {
                                                        skin.contains("Viper") -> "M4A1 Core: High rate of bullet frequency trail."
                                                        skin.contains("Drake") -> "AWP Core: Flame red dragon scope & burst trail."
                                                        skin.contains("Shahi") -> "Desert Eagle: Royal Rajput Lahore gold alloys +30 dmg."
                                                        else -> "Classic Slate: Factory standard default military metal."
                                                    },
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 7.5.sp,
                                                    lineHeight = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            
                                            // Determine weapon target
                                            val wpType = when {
                                                skin.contains("M4A1") -> "M4A1"
                                                skin.contains("AWP") -> "AWP"
                                                else -> "DEAGLE"
                                            }
                                            
                                            Button(
                                                onClick = {
                                                    viewModel.changeWeaponSkin(wpType, skin)
                                                    Toast.makeText(context, "$skin equipped on $wpType!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isAnyEquipped) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.08f)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(26.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (isAnyEquipped) "EQUIPPED" else "EQUIP",
                                                    fontSize = 8.sp,
                                                    color = if (isAnyEquipped) Color.Black else Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. LUCK ROYALE & CHARACTER STORE OVERLAY
        if (showStoreDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .clickable { showStoreDialog = false }
                    .testTag("modal_store"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF0F1115),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, DeadShotTheme.GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(DeadShotTheme.GoldAccent, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚡ DEAD SHOT LUCK ROYALE & FACTIONS",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            // Tab Selectors
                            Row(
                                modifier = Modifier
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val tabs = listOf("ROYALE" to "🍀 LUCK ROYALE", "CHARACTERS" to "👤 HEROES", "BUNDLES" to "📦 BUNDLES")
                                tabs.forEach { (tag, label) ->
                                    val isSel = storeActiveTab == tag
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSel) DeadShotTheme.GoldAccent else Color.Transparent,
                                                RoundedCornerShape(3.dp)
                                            )
                                            .clickable { storeActiveTab = tag }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showStoreDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("CLOSE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Content according to tabs
                        when (storeActiveTab) {
                            "ROYALE" -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Left Column: The Interactive Spin Machine Wheel
                                    Column(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .fillMaxHeight()
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "SPIN LUCK SPIN WHEEL",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DeadShotTheme.GoldAccent,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "SPEND DSG FOR LEGENDARY CRATE WEAPON SKIN REWARDS!",
                                            fontSize = 7.sp,
                                            color = Color.LightGray,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        // Spin Wheel Canvas Visuals
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val angleTransition = rememberInfiniteTransition()
                                            val spinDegree by angleTransition.animateFloat(
                                                initialValue = 0f,
                                                targetValue = 360f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(if (isSpinningRoyale) 350 else 7000, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Restart
                                                )
                                            )

                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                rotate(spinDegree) {
                                                    // Draw colorful segment sections
                                                    val colors = listOf(Color(0xFFFF3366), Color(0xFF33CCFF), Color(0xFFFFCC00), Color(0xFF33FF66), Color(0xFF9933FF), Color(0xFFFF9900))
                                                    val sliceAngle = 360f / colors.size
                                                    for (idx in colors.indices) {
                                                        drawArc(
                                                            color = colors[idx],
                                                            startAngle = idx * sliceAngle,
                                                            sweepAngle = sliceAngle,
                                                            useCenter = true
                                                        )
                                                    }
                                                    // Inner rim
                                                    drawCircle(Color.Black.copy(alpha = 0.5f), radius = size.minDimension * 0.42f)
                                                }
                                                // Center peg
                                                drawCircle(Color.White, radius = 8f)
                                                drawCircle(DeadShotTheme.GoldAccent, radius = 5f)
                                            }

                                            // Pointer arrow indicatior
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .offset(y = (-6).dp)
                                                    .size(14.dp)
                                                    .background(Color.White, RoundedCornerShape(3.dp))
                                                    .border(1.dp, Color.Red, RoundedCornerShape(3.dp))
                                            )
                                        }

                                        // Draw banner or result
                                        if (lastPrizeDrawn.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF231E12), RoundedCornerShape(6.dp))
                                                    .border(1.dp, DeadShotTheme.GoldAccent, RoundedCornerShape(6.dp))
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("🔥 UNLOCKED REWARD:", color = DeadShotTheme.GoldAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(lastPrizeDrawn.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("AUTO-EQUIPPED IN VAULT CABINET", color = Color.LightGray, fontSize = 6.sp)
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "SPIN COST: 100 DSG GEMS",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (dsgCurrency >= 100) {
                                                    isSpinningRoyale = true
                                                    coroutineScope.launch {
                                                        delay(1200)
                                                        isSpinningRoyale = false
                                                        viewModel.spinLuckRoyale { prize ->
                                                            lastPrizeDrawn = prize
                                                            Toast.makeText(context, "LUCK DRAW WIN: $prize!", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Insufficient DSG balance! Keep training or claim mail bonuses.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = !isSpinningRoyale && dsgCurrency >= 100,
                                            colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.GoldAccent, disabledContainerColor = Color.DarkGray),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                        ) {
                                            Text(
                                                text = if (isSpinningRoyale) "RAFFLING CRATES..." else "SPIN SINGLE (100 DSG)",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    // Right Column: List of jackpot prizes available in pool
                                    Column(
                                        modifier = Modifier.weight(0.9f)
                                    ) {
                                        Text(
                                            text = "💎 ACTIVE PRIZEPOOL IN CRATE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val potentialRewards = listOf(
                                                "Neon Viper M4A1" to "⚡ Neon green speed reload tracking barrel.",
                                                "Crimson Drake AWP" to "🔥 Flame red dragon custom optical muzzle blast.",
                                                "Luxe Shahi Deagle" to "👑 Elegant Lahore Shahi gold gold alloy plating.",
                                                "Angel Suit Outfit" to "🛡️ Legendary angel feathered back wing thruster.",
                                                "Bonus +500 DSG!" to "💰 Instant rich payload boost directly in gems container."
                                            )
                                            items(potentialRewards) { (name, info) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                                        .padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(Color(0xFF271B1E), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("🎁", fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(name.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                        Text(info, color = Color.Gray, fontSize = 7.sp, lineHeight = 8.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "CHARACTERS" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "SELECT HERO SPECIALISTS • FACTION CAPABILITY SELECTION",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val characterPool = listOf(
                                            Triple("DJ Alok", "🎵 DROP THE BEAT", "Pulsates 5m cyan aura healing status in Arena on active skill usage (+30 HP). Special music audio circle waves!"),
                                            Triple("Chrono", "🌀 TIME TURNER", "Creates active sphere dome shield in battle. Eliminates heavy environment wind drift to absolute 0f!"),
                                            Triple("Kelly", "⚡ DASH RUNNER", "Passive ultra hyper speed dash. Decreases scope target drift and enables instant aim precision.")
                                        )

                                        characterPool.forEach { (cName, skillLabel, sDesc) ->
                                            val isEq = equippedCharacter == cName
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable { viewModel.equipCharacter(cName) }
                                                    .border(
                                                        width = if (isEq) 2.dp else 1.dp,
                                                        color = if (isEq) DeadShotTheme.GoldAccent else Color.White.copy(alpha = 0.05f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isEq) Color(0xFF191712) else Color(0xFF121418)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(10.dp),
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(cName.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        if (isEq) DeadShotTheme.GoldAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                                                        CircleShape
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isEq) "EQUIPPED" else "READY",
                                                                    color = if (isEq) DeadShotTheme.GoldAccent else Color.Gray,
                                                                    fontSize = 6.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(skillLabel, color = DeadShotTheme.GoldAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(sDesc, color = Color.White.copy(alpha = 0.5f), fontSize = 7.5.sp, lineHeight = 10.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.equipCharacter(cName)
                                                            Toast.makeText(context, "HERO EQUIPPED: $cName. READY FOR DEPLOYMENT!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isEq) DeadShotTheme.GoldAccent else Color.White.copy(alpha = 0.08f)
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(28.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isEq) "ACTIVE COMMAND" else "SELECT HERO",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isEq) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // SKILL SLOTS MANAGEMENT SYSTEM DESK
                                    Text(
                                        text = "⚡ HERO FORCE ACTIVE & PASSIVE SKILL GEAR SLOTS",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(58.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // ACTIVE SKILL CARD
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clickable {
                                                    expandedActiveSkillSelector = !expandedActiveSkillSelector
                                                    expandedPassiveSkillSelector = false
                                                }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (expandedActiveSkillSelector) DeadShotTheme.GoldAccent else Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E12))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(DeadShotTheme.GoldAccent.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = when (equippedActiveSkill) {
                                                            "Drop the Beat" -> "🎵"
                                                            "Time Turner" -> "🛡️"
                                                            "Deadly Velocity" -> "⚡"
                                                            "Master of All" -> "☸️"
                                                            else -> "⭐"
                                                        },
                                                        fontSize = 14.sp
                                                     )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("ACTIVE ABILITY SLOT", color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Text(equippedActiveSkill.uppercase(), color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                                                    Text("Click to swap active spell", color = DeadShotTheme.GoldAccent.copy(alpha = 0.8f), fontSize = 5.5.sp, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }

                                        // PASSIVE SKILL CARD
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clickable {
                                                    expandedPassiveSkillSelector = !expandedPassiveSkillSelector
                                                    expandedActiveSkillSelector = false
                                                }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (expandedPassiveSkillSelector) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E12))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(DeadShotTheme.LaserGreen.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = when (equippedPassiveSkill) {
                                                            "Maxim's Gluttony" -> "🩹"
                                                            "Moco's Hacker Eye" -> "👁️"
                                                            "Andrew's Vest Protection" -> "🦺"
                                                            else -> "🛡️"
                                                        },
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("PASSIVE BUFF SLOT", color = Color.Gray, fontSize = 6.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Text(equippedPassiveSkill.uppercase(), color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                                                    Text("Click to swap passive buff", color = DeadShotTheme.LaserGreen.copy(alpha = 0.8f), fontSize = 5.5.sp, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }

                                    // ACTIVE SKILL DRAWER PANEL
                                    if (expandedActiveSkillSelector) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14161C)),
                                            border = BorderStroke(1.dp, DeadShotTheme.GoldAccent.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text("CHOOSE ACTIVE ABILITY TO EQUIP INSIDE COMBAT GEAR:", color = DeadShotTheme.GoldAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val actives = listOf(
                                                    Triple("Drop the Beat", "🎵 Aura Heal", "Restores +25 HP/sec while aura triggered! Cooldown 35s."),
                                                    Triple("Time Turner", "🛡️ Field Shield", "Creates dome forcefield blocks. Bypasses sandstorm drifts completely!"),
                                                    Triple("Deadly Velocity", "⚡ Speed Spark", "Increases sensitivity speed by +50% and bullet deals double 2x damage!"),
                                                    Triple("Master of All", "☸️ EP Master", "Recovers EP to 250 instantly and grants +40 HP health aura burst.")
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    actives.forEach { (skillName, label, desc) ->
                                                        val isSelected = equippedActiveSkill == skillName
                                                        Card(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(52.dp)
                                                                .clickable {
                                                                    viewModel.equipActiveSkill(skillName)
                                                                    Toast.makeText(context, "ACTIVE SLOT: $skillName equipped!", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .border(
                                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                                    color = if (isSelected) DeadShotTheme.GoldAccent else Color.White.copy(alpha = 0.05f),
                                                                    shape = RoundedCornerShape(6.dp)
                                                                ),
                                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF221F18) else Color(0xFF0F1115))
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                                                verticalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(label, color = if (isSelected) DeadShotTheme.GoldAccent else Color.White, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                                Text(desc, color = Color.Gray, fontSize = 5.2.sp, lineHeight = 6.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // PASSIVE SKILL DRAWER PANEL
                                    if (expandedPassiveSkillSelector) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14161C)),
                                            border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text("CHOOSE PASSIVE ENHANCEMENT TRAIT TO ENHANCE SOLDIER:", color = DeadShotTheme.LaserGreen, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val passives = listOf(
                                                    Triple("Maxim's Gluttony", "🩹 Quick Heal", "Cuts therapeutic healing medkit timer by 50% down to 1.5 seconds!"),
                                                    Triple("Moco's Hacker Eye", "👁️ Hacker Tag", "Tags struck targets with a bright holographic hacker pulse for 4s!"),
                                                    Triple("Andrew's Vest Protection", "🦺 Andrew Vest", "Raises absolute maximum player HP boundaries ceiling to 240 HP!")
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    passives.forEach { (skillName, label, desc) ->
                                                        val isSelected = equippedPassiveSkill == skillName
                                                        Card(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(52.dp)
                                                                .clickable {
                                                                    viewModel.equipPassiveSkill(skillName)
                                                                    Toast.makeText(context, "PASSIVE SLOT: $skillName equipped!", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .border(
                                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                                    color = if (isSelected) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.05f),
                                                                    shape = RoundedCornerShape(6.dp)
                                                                ),
                                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF16201B) else Color(0xFF0F1115))
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                                                verticalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(label, color = if (isSelected) DeadShotTheme.LaserGreen else Color.White, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                                Text(desc, color = Color.Gray, fontSize = 5.2.sp, lineHeight = 6.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "BUNDLES" -> {
                                Column {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0x2239FF14)),
                                        border = BorderStroke(1.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            viewModel.claimMidnightBonus()
                                            Toast.makeText(context, "120 DSG claimed!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("⏰ 12:00 MIDNIGHT RECHARGE DSG CLAIMS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Text("Claim free daily midnight reload bonus of 120 DSG instantly", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.claimMidnightBonus()
                                                    Toast.makeText(context, "120 DSG claimed!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.LaserGreen),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("CLAIM +120 DSG", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(Color(0xFF2C1E21), CircleShape)
                                                .border(2.dp, Color(0xFFFF5252), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("😇", fontSize = 32.sp)
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("ANGEL SUIT OVERPOWER BUNDLE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                            Text("OFFER EXPIRY: 10 DAYS LIMIT", color = Color(0xFFFF5252), fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text("Unlocks premium feathered armor in the Vault clothing suite. Equips angelic golden-gilded thrusters.", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        if (isDailyLimitAngelBundlePurchased) {
                                            Button(
                                                onClick = {},
                                                enabled = false,
                                                colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.DarkGray),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("OWNED", fontSize = 10.sp, color = Color.LightGray)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    val success = viewModel.purchaseAngelBundle(800)
                                                    if (success) {
                                                        Toast.makeText(context, "Angel Bundle Purchased! Equipped in Vault.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "Insufficient DSG! (Needs 800 DSG)", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("BUY BUNDLE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("💎 800 DSG", fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. STAINLESS SATELLITE TACTICAL MAP SELECTION DIRECTORY (4K ULTRA ACCENT)
        if (showMapSelectionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .clickable { showMapSelectionDialog = false }
                    .testTag("modal_map_selection"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF0F1115),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, DeadShotTheme.LaserGreen),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(DeadShotTheme.LaserGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PAKISTAN BATTLEGROUND TACTICAL DIRECTOR",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "SELECT RE-ENTRY CODENAME FIELD SECTOR • SATELLITE LINK SYNC ACTIVE",
                                    color = DeadShotTheme.LaserGreen.copy(alpha = 0.7f),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                )
                            }
                            Button(
                                onClick = { showMapSelectionDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.TacticalRed),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("CLOSE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 3 Maps side-by-side!
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PakistanMap.values().forEach { map ->
                                val isSelected = selectedMap == map
                                val urduText = when(map) {
                                    PakistanMap.KARACHI -> "کراچی پورٹ"
                                    PakistanMap.LAHORE -> "شاہی قلعہ لاہور"
                                    PakistanMap.ISLAMABAD -> "اسلام آباد مارگلہ"
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            viewModel.selectMap(map)
                                        }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.06f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF132014) else Color(0xFF14161B)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            // Top row coordinates & index
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when(map) {
                                                        PakistanMap.KARACHI -> "LAT: 24.86° N | LON: 67.00° E"
                                                        PakistanMap.LAHORE -> "LAT: 31.58° N | LON: 74.32° E"
                                                        PakistanMap.ISLAMABAD -> "LAT: 33.72° N | LON: 73.04° E"
                                                    },
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontSize = 7.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (isSelected) DeadShotTheme.LaserGreen.copy(alpha = 0.2f)
                                                            else Color.White.copy(alpha = 0.04f),
                                                            CircleShape
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isSelected) "ACTIVE" else "STANDBY",
                                                        color = if (isSelected) DeadShotTheme.LaserGreen else Color.Gray,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Styled Map Name & Urdu
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = map.label.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = urduText,
                                                    color = if (isSelected) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Map Spec Canvas visualizer!
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    // Draw custom coordinate satellite radar scan lines for visual wow!
                                                    val w = size.width
                                                    val h = size.height
                                                    val pulse = h * 0.45f
                                                    
                                                    // Holographic horizontal sweeping laser line
                                                    drawLine(
                                                        color = DeadShotTheme.LaserGreen.copy(alpha = 0.12f),
                                                        start = Offset(0f, h * 0.5f),
                                                        end = Offset(w, h * 0.5f),
                                                        strokeWidth = 2f
                                                    )
                                                    
                                                    drawCircle(
                                                        color = if (isSelected) DeadShotTheme.LaserGreen.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f),
                                                        radius = pulse,
                                                        center = center
                                                    )
                                                    drawCircle(
                                                        color = if (isSelected) DeadShotTheme.LaserGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                                        radius = pulse * 0.6f,
                                                        center = center,
                                                        style = Stroke(width = 1f)
                                                    )
                                                    
                                                    // Small scan dot indicator
                                                    val dotX = when(map) {
                                                        PakistanMap.KARACHI -> w * 0.3f
                                                        PakistanMap.LAHORE -> w * 0.5f
                                                        PakistanMap.ISLAMABAD -> w * 0.7f
                                                    }
                                                    val dotY = h * 0.5f
                                                    drawCircle(
                                                        color = if (isSelected) Color(0xFF39FF14) else Color.Red,
                                                        radius = 4f,
                                                        center = Offset(dotX, dotY)
                                                    )
                                                }
                                                Text(
                                                    text = when(map) {
                                                        PakistanMap.KARACHI -> "⚓ HARBOR CONTAINERS REGION"
                                                        PakistanMap.LAHORE -> "🏰 ROYAL RAJPUT RESIDENCY"
                                                        PakistanMap.ISLAMABAD -> "⛰️ MARGALLA SCENIC DEFENSE GAP"
                                                    },
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontSize = 7.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Map Long description
                                            Text(
                                                text = map.desc,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 9.sp,
                                                lineHeight = 11.sp,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                        }

                                        Column {
                                            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                                            // Interactive selector or equipment indicators
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "THREAT MATRIX",
                                                        fontSize = 7.sp,
                                                        color = Color.White.copy(alpha = 0.4f),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = when(map) {
                                                            PakistanMap.KARACHI -> "EASY (50 ENEMIES)"
                                                            PakistanMap.LAHORE -> "SO BUT HARD (50 ENEMIES)"
                                                            PakistanMap.ISLAMABAD -> "HARD (100 ENEMIES)"
                                                        },
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when(map) {
                                                            PakistanMap.KARACHI -> Color(0xFF00FF87)
                                                            PakistanMap.LAHORE -> Color(0xFFFFC107)
                                                            PakistanMap.ISLAMABAD -> Color(0xFFFF3D00)
                                                        },
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.selectMap(map)
                                                        Toast.makeText(context, "DEPLOY FIELD SECURED: ${map.label.uppercase()}", Toast.LENGTH_SHORT).show()
                                                        showMapSelectionDialog = false
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSelected) DeadShotTheme.LaserGreen else Color.White.copy(alpha = 0.08f)
                                                    ),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.height(28.dp).testTag("select_map_${map.name}")
                                                ) {
                                                    Text(
                                                        text = if (isSelected) "EQUIPPED" else "DEPLOY HERE",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.Black else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(title: String, desc: String, unlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (unlocked) DeadShotTheme.LaserGreen else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(desc, color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = if (unlocked) "UNLOCKED" else "LOCKED",
            color = if (unlocked) DeadShotTheme.LaserGreen else Color.DarkGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
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

    // RETRIEVED HUD SETTING STATES
    val sensitivityVal by viewModel.sensitivityVal.collectAsState()
    val redDotSizeVal by viewModel.redDotSizeVal.collectAsState()
    val joystickScaleVal by viewModel.joystickScaleVal.collectAsState()
    val fireButtonScaleVal by viewModel.fireButtonScaleVal.collectAsState()

    val currentAmmo by viewModel.currentAmmo.collectAsState()
    val isReloading by viewModel.isReloading.collectAsState()
    val isFiring by viewModel.isFiring.collectAsState()
    val recoilOffset by viewModel.recoilOffset.collectAsState()
    val isScoped by viewModel.isScoped.collectAsState()
    val scenicTargets by viewModel.scenicTargets.collectAsState()
    val score by viewModel.score.collectAsState()

    // ACTIVE FREE FIRE MATCHMAKING ENGINE STATES
    val survivorsCount by viewModel.survivorsCount.collectAsState()
    val arenaKills by viewModel.arenaKills.collectAsState()
    val battleLogFeed by viewModel.battleLogFeed.collectAsState()
    val profileName by viewModel.profileName.collectAsState()
    val dsgCurrency by viewModel.dsgCurrency.collectAsState()
    val equippedCharacter by viewModel.equippedCharacter.collectAsState()
    val equippedActiveSkill by viewModel.equippedActiveSkill.collectAsState()
    val equippedPassiveSkill by viewModel.equippedPassiveSkill.collectAsState()
    val mocoTaggedTargetId by viewModel.mocoTaggedTargetId.collectAsState()
    val isSkillActive by viewModel.isSkillActive.collectAsState()
    val skillCooldown by viewModel.skillCooldown.collectAsState()
    val equippedWeaponSkinsByWp by viewModel.equippedWeaponSkins.collectAsState()

    // FREE FIRE SYSTEM INTEGRATED ACTIVE STATE LIFECYCLES
    val playerHp by viewModel.playerHp.collectAsState()
    val playerEp by viewModel.playerEp.collectAsState()
    val glooWallCount by viewModel.glooWallCount.collectAsState()
    val isGlooWallDeployed by viewModel.isGlooWallDeployed.collectAsState()
    val glooWallHp by viewModel.glooWallHp.collectAsState()
    val glooWallX by viewModel.glooWallX.collectAsState()
    val glooWallY by viewModel.glooWallY.collectAsState()
    val glooWallSecondsLeft by viewModel.glooWallSecondsLeft.collectAsState()
    val isBooyahActive by viewModel.isBooyahActive.collectAsState()
    val medkitCount by viewModel.medkitCount.collectAsState()
    val isChannelingHeal by viewModel.isChannelingHeal.collectAsState()
    val healProgress by viewModel.healProgress.collectAsState()

    val mushroomX by viewModel.mushroomX.collectAsState()
    val mushroomY by viewModel.mushroomY.collectAsState()
    val isMushroomVisible by viewModel.isMushroomVisible.collectAsState()

    val airDropX by viewModel.airDropX.collectAsState()
    val airDropY by viewModel.airDropY.collectAsState()
    val isAirDropInbound by viewModel.isAirDropInbound.collectAsState()
    val isAirDropLanded by viewModel.isAirDropLanded.collectAsState()
    val isAirDropLooted by viewModel.isAirDropLooted.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Impact notifications floating message popup
    var combatFeedbackMessage by remember { mutableStateOf("") }
    var feedbackTriggerToggle by remember { mutableStateOf(false) }

    val glooWallScale = remember { Animatable(0f) }
    LaunchedEffect(isGlooWallDeployed) {
        if (isGlooWallDeployed) {
            glooWallScale.snapTo(0f)
            glooWallScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            glooWallScale.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            )
        }
    }

    // Touch/Aim parameters
    var crosshairX by remember { mutableStateOf(400f) }
    var crosshairY by remember { mutableStateOf(240f) }

    // Particle rain / wind loop and dynamic weather positioning controls
    val simulationFrame = remember { mutableStateOf(0) }
    LaunchedEffect(scenicTargets, selectedWeather, isSkillActive, equippedActiveSkill) {
        while (true) {
            delay(30)
            simulationFrame.value += 1
            // Perform automatic targets horizontal drifting if Islamabad hills active
            viewModel.updateMovingTargets(800f)

            // DUST SANDSTORM ACTIVE: Aim sway drift (Bypassed if Time Turner barrier shield is active!)
            if (selectedWeather == WeatherType.SANDSTORM && !(equippedActiveSkill == "Time Turner" && isSkillActive)) {
                val swayX = (sin(simulationFrame.value * 0.08f) * 1.5f - 0.4f)
                val swayY = (cos(simulationFrame.value * 0.06f) * 1.1f)
                crosshairX = (crosshairX + swayX).coerceIn(40f, 760f)
                crosshairY = (crosshairY + swayY).coerceIn(40f, 380f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(selectedWeather, isSkillActive, equippedActiveSkill, sensitivityVal) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Dynamic physics-based control speed (Kelly/Alok skills speeds up, Monsoon Rain slows down)
                        val multiplier = when {
                            equippedActiveSkill == "Deadly Velocity" && isSkillActive -> 1.50f  // +50% sprint boost speed
                            equippedActiveSkill == "Drop the Beat" && isSkillActive -> 1.25f // +25% tempo boost speed
                            selectedWeather == WeatherType.MONSOON -> 0.70f          // -30% rain drag sluggishness
                            else -> 1.0f
                        }
                        val finalSpeed = sensitivityVal * multiplier
                        // Move aiming crosshair offset smoothly inside valid dimensions with customizable sensitivity
                        crosshairX = (crosshairX + dragAmount.x * finalSpeed).coerceIn(40f, 760f)
                        crosshairY = (crosshairY + dragAmount.y * finalSpeed).coerceIn(40f, 380f)
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

                        // Moco's Hacker Eye scanning hud indicator
                        if (mocoTaggedTargetId == target.id) {
                            val pulseMoco = 1.0f + 0.15f * sin(simulationFrame.value * 0.2f)
                            drawCircle(
                                color = Color(0xFFFF5722).copy(alpha = 0.5f), // Neon orange tag glow
                                radius = (target.size / 2 + 12f) * pulseMoco,
                                center = Offset(centerX, centerY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                            )
                            val len = 10f
                            val offsetDist = (target.size / 2) + 8f
                            drawLine(Color(0xFFFF3333), Offset(centerX - offsetDist, centerY - offsetDist), Offset(centerX - offsetDist + len, centerY - offsetDist), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX - offsetDist, centerY - offsetDist), Offset(centerX - offsetDist, centerY - offsetDist + len), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX + offsetDist, centerY - offsetDist), Offset(centerX + offsetDist - len, centerY - offsetDist), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX + offsetDist, centerY - offsetDist), Offset(centerX + offsetDist, centerY - offsetDist + len), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX - offsetDist, centerY + offsetDist), Offset(centerX - offsetDist + len, centerY + offsetDist), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX - offsetDist, centerY + offsetDist), Offset(centerX - offsetDist, centerY + offsetDist - len), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX + offsetDist, centerY + offsetDist), Offset(centerX + offsetDist - len, centerY + offsetDist), 2f)
                            drawLine(Color(0xFFFF3333), Offset(centerX + offsetDist, centerY + offsetDist), Offset(centerX + offsetDist, centerY + offsetDist - len), 2f)
                        }

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

                // GARENA FREE FIRE GOLDEN MUSHROOM (🍄 LEVEL 4) DEFINITION
                if (isMushroomVisible) {
                    val scaleMX = mushroomX / 800f * w
                    val scaleMY = (mushroomY / 450f * h).coerceAtLeast(h * 0.5f)
                    
                    // Golden ground indicator glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFA000).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(scaleMX, scaleMY),
                            radius = 32f
                        ),
                        radius = 32f,
                        center = Offset(scaleMX, scaleMY)
                    )
                    
                    // Mushroom stem (White vertical rectangle)
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(scaleMX - 5f, scaleMY - 10f),
                        size = Size(10f, 20f)
                    )
                    
                    // Mushroom cap (Golden/Orange semi-circle)
                    drawArc(
                        color = Color(0xFFFFB300),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(scaleMX - 20f, scaleMY - 22f),
                        size = Size(40f, 24f)
                    )

                    // Draw spots on mushroom cap
                    drawCircle(Color.White, radius = 3f, center = Offset(scaleMX - 8f, scaleMY - 16f))
                    drawCircle(Color.White, radius = 2.5f, center = Offset(scaleMX + 8f, scaleMY - 14f))
                    drawCircle(Color.White, radius = 3f, center = Offset(scaleMX, scaleMY - 20f))
                }

                // GARENA FREE FIRE SUPPLY AIRDROP (📦 WITH YELLOW LASER SKY indicator BEACON)
                if (isAirDropInbound || isAirDropLanded) {
                    val scaleADX = airDropX / 800f * w
                    val scaleADY = airDropY / 450f * h
                    
                    if (isAirDropLanded) {
                        // Cargo drops resting on the ground
                        
                        // Yellow sky laser light column (signature extreme high visibility marker)
                        if (!isAirDropLooted) {
                            val laserPulse = 0.45f + 0.2f * sin(simulationFrame.value * 0.15f)
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFFFFD54F).copy(alpha = laserPulse), Color.Transparent)
                                ),
                                topLeft = Offset(scaleADX - 16f, 0f),
                                size = Size(32f, scaleADY)
                            )
                        }
                        
                        if (!isAirDropLooted) {
                            // Red steel supply container container
                            drawRect(
                                color = Color(0xFFC62828),
                                topLeft = Offset(scaleADX - 22f, scaleADY - 18f),
                                size = Size(44f, 32f)
                            )
                            // Blue supply protective tarp top
                            drawRect(
                                color = Color(0xFF1565C0),
                                topLeft = Offset(scaleADX - 25f, scaleADY - 23f),
                                size = Size(50f, 10f)
                            )
                            // Beacon gold core glow
                            drawCircle(
                                color = Color(0xFFFFD54F).copy(alpha = 0.3f),
                                radius = 35f,
                                center = Offset(scaleADX, scaleADY),
                                style = Stroke(width = 1.5f)
                            )
                        } else {
                            // Draw looted grey target
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.4f),
                                topLeft = Offset(scaleADX - 22f, scaleADY - 18f),
                                size = Size(44f, 32f)
                            )
                            drawRect(
                                color = Color.DarkGray.copy(alpha = 0.5f),
                                topLeft = Offset(scaleADX - 25f, scaleADY - 23f),
                                size = Size(50f, 10f)
                            )
                        }
                    } else if (isAirDropInbound) {
                        // Parachute cargo descending from skies
                        
                        // Red box
                        drawRect(
                            color = Color(0xFFC62828),
                            topLeft = Offset(scaleADX - 14f, scaleADY - 10f),
                            size = Size(28f, 20f)
                        )
                        drawRect(
                            color = Color(0xFF1565C0),
                            topLeft = Offset(scaleADX - 16f, scaleADY - 15f),
                            size = Size(32f, 6f)
                        )
                        
                        // Straps
                        drawLine(Color(0xFF424242), Offset(scaleADX - 10f, scaleADY - 15f), Offset(scaleADX - 20f, scaleADY - 38f), strokeWidth = 2f)
                        drawLine(Color(0xFF424242), Offset(scaleADX + 10f, scaleADY - 15f), Offset(scaleADX + 20f, scaleADY - 38f), strokeWidth = 2f)
                        
                        // Parachute canopy dome
                        drawArc(
                            color = Color.Black.copy(alpha = 0.85f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(scaleADX - 26f, scaleADY - 55f),
                            size = Size(52f, 32f)
                        )
                    }
                }

                // FREE FIRE GLOO WALL (ENERGY FREEZING ICE SHIELD BARRIER)
                if (glooWallScale.value > 0f) {
                    val scaleFactor = glooWallScale.value
                    val wallPulse = (0.70f + 0.18f * sin(simulationFrame.value * 0.15f)) * scaleFactor
                    
                    // 4K Ultra Curved Garena Ice Crest Shield Path centered on (glooWallX, glooWallY)
                    val baseW = 160f * scaleFactor
                    val baseH = 90f * scaleFactor
                    
                    val leftX = glooWallX - baseW / 2
                    val rightX = glooWallX + baseW / 2
                    val topY = glooWallY - baseH / 2
                    val bottomY = glooWallY + baseH / 2
                    
                    // Outer high-fidelity drop glow aura
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.22f * wallPulse),
                        radius = 95f * scaleFactor,
                        center = Offset(glooWallX, glooWallY)
                    )
                    
                    // Curved geometric ice crest segment Path
                    val crestPath = Path().apply {
                        moveTo(leftX, bottomY - 10f * scaleFactor)
                        quadraticTo(glooWallX, bottomY + 15f * scaleFactor, rightX, bottomY - 10f * scaleFactor)
                        lineTo(rightX + 10f * scaleFactor, topY + 15f * scaleFactor)
                        quadraticTo(glooWallX, topY - 15f * scaleFactor, leftX - 10f * scaleFactor, topY + 15f * scaleFactor)
                        close()
                    }
                    
                    // 1. Fill 3D Glacier crystal plates with premium shiny metallic gradients
                    drawPath(
                        path = crestPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE0F7FA).copy(alpha = 0.85f * scaleFactor),
                                Color(0xFF4DD0E1).copy(alpha = 0.75f * wallPulse),
                                Color(0xFF00ACC1).copy(alpha = 0.85f * scaleFactor),
                                Color(0xFF006064).copy(alpha = 0.95f * scaleFactor)
                            )
                        )
                    )
                    
                    // 2. High-Tech neon edge contour lines
                    drawPath(
                        path = crestPath,
                        color = Color(0xFF00F0FF).copy(alpha = wallPulse),
                        style = Stroke(width = 3.5f * scaleFactor)
                    )
                    
                    // 3. Cyber grid lines mirroring digital cellular hexagonal structure
                    val linesCount = 6
                    for (i in 1..linesCount) {
                        val fraction = i.toFloat() / (linesCount + 1)
                        val currX = leftX + baseW * fraction
                        drawLine(
                            color = Color(0xFFB2EBF2).copy(alpha = 0.35f * scaleFactor),
                            start = Offset(currX, topY + 10f * scaleFactor),
                            end = Offset(currX, bottomY),
                            strokeWidth = 1.5f * scaleFactor
                        )
                        // Diagonal crossing
                        drawLine(
                            color = Color(0xFFB2EBF2).copy(alpha = 0.20f * scaleFactor),
                            start = Offset(leftX, topY + (baseH * fraction)),
                            end = Offset(rightX, bottomY - (baseH * fraction)),
                            strokeWidth = 1f * scaleFactor
                        )
                    }
                    
                    // 4. Procedural cracking/fracture lines depending dynamically on Gloo Wall remaining HP
                    val damageRatio = (150f - glooWallHp) / 150f
                    if (damageRatio > 0.05f) {
                        val crackColor = Color(0xFFFF5252).copy(alpha = 0.85f)
                        val sparkPulse = (0.8f + 0.2f * sin(simulationFrame.value * 0.3f)) * scaleFactor
                        // Crack branching
                        val branches = (1 + (damageRatio * 8).toInt()).coerceAtMost(8)
                        for (b in 0 until branches) {
                            val angle = b * (360f / branches) + (simulationFrame.value * 0.1f)
                            val rad = Math.toRadians(angle.toDouble())
                            val length = 60f * damageRatio * scaleFactor
                            val targetX = glooWallX + (length * cos(rad) * sparkPulse).toFloat()
                            val targetY = glooWallY + (length * sin(rad) * sparkPulse).toFloat()
                            drawLine(
                                color = crackColor,
                                start = Offset(glooWallX, glooWallY),
                                end = Offset(targetX, targetY),
                                strokeWidth = 3f * damageRatio * scaleFactor
                            )
                            // Sub-branches
                            if (damageRatio > 0.4f) {
                                val subRad = Math.toRadians((angle + 35f).toDouble())
                                val subLength = 30f * damageRatio * scaleFactor
                                drawLine(
                                    color = Color(0xFFFF8A80).copy(alpha = 0.9f),
                                    start = Offset((glooWallX + length * 0.5f * cos(rad)).toFloat(), (glooWallY + length * 0.5f * sin(rad)).toFloat()),
                                    end = Offset((glooWallX + length * 0.5f * cos(rad) + subLength * cos(subRad)).toFloat(), (glooWallY + length * 0.5f * sin(rad) + subLength * sin(subRad)).toFloat()),
                                    strokeWidth = 1.5f * damageRatio * scaleFactor
                                )
                            }
                        }
                    }
                    
                    // 5. Drawing 10s Circular Hologram Timer Dial above the shield
                    val dialCenter = Offset(glooWallX, glooWallY - 72f * scaleFactor)
                    val dialRadius = 24f * scaleFactor
                    
                    // Dial frame base background
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.75f * scaleFactor),
                        radius = dialRadius,
                        center = dialCenter
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f * scaleFactor),
                        radius = dialRadius,
                        center = dialCenter,
                        style = Stroke(width = 3.5f * scaleFactor)
                    )
                    
                    // Neon Cyan remaining seconds arc loader
                    val sweepProgress = (glooWallSecondsLeft.toFloat() / 10f) * 360f
                    drawArc(
                        color = if (glooWallSecondsLeft > 3) Color(0xFF00FFCC).copy(alpha = scaleFactor) else Color(0xFFFF3D00).copy(alpha = scaleFactor),
                        startAngle = -90f,
                        sweepAngle = sweepProgress,
                        useCenter = false,
                        topLeft = Offset(dialCenter.x - dialRadius, dialCenter.y - dialRadius),
                        size = Size(dialRadius * 2, dialRadius * 2),
                        style = Stroke(width = 3.5f * scaleFactor)
                    )
                    
                    // Crisp 4K digital seconds count text inside dial
                    if (scaleFactor > 0.3f) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "${glooWallSecondsLeft}s",
                            glooWallX,
                            glooWallY - 65f * scaleFactor,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 21f * scaleFactor
                                typeface = android.graphics.Typeface.MONOSPACE
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }
                    
                    // 6. Cybernetic durability linear health progress bar
                    val barW = 110f * scaleFactor
                    val barH = 5f * scaleFactor
                    val barLeft = glooWallX - barW / 2
                    val barTop = glooWallY - 104f * scaleFactor
                    
                    // Durability text marker
                    if (scaleFactor > 0.4f) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "ICE COVER HP: $glooWallHp/150",
                            glooWallX,
                            glooWallY - 114f * scaleFactor,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GREEN
                                textSize = 14f * scaleFactor
                                typeface = android.graphics.Typeface.MONOSPACE
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                    
                    // Bar Border Backdrop
                    drawRect(
                        color = Color.Black.copy(alpha = 0.65f * scaleFactor),
                        topLeft = Offset(barLeft - 2f * scaleFactor, barTop - 2f * scaleFactor),
                        size = Size(barW + 4f * scaleFactor, barH + 4f * scaleFactor)
                    )
                    // Bar Fill progress ratio
                    val hpFrac = (glooWallHp.toFloat() / 150f).coerceIn(0f, 1f)
                    val barColor = when {
                        hpFrac > 0.5f -> Color(0xFF00FFCC).copy(alpha = scaleFactor)
                        hpFrac > 0.25f -> Color(0xFFFFEB3B).copy(alpha = scaleFactor)
                        else -> Color(0xFFFF3D00).copy(alpha = scaleFactor)
                    }
                    drawRect(
                        color = barColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barW * hpFrac, barH)
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

                // CHARACTER ACTIVE OVERLAY EFFECTS
                if (isSkillActive) {
                    when (equippedCharacter) {
                        "DJ Alok" -> {
                            val waveCount = 3
                            for (i in 0 until waveCount) {
                                val phase = (simulationFrame.value * 2f + i * 40f) % 120f
                                drawCircle(
                                    color = Color(0xFF00E5FF).copy(alpha = (1f - phase / 120f) * 0.4f),
                                    radius = 100f + phase * 2.5f,
                                    center = Offset(w * 0.5f, h * 0.8f),
                                    style = Stroke(width = 3f)
                                )
                            }
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                radius = 280f,
                                center = Offset(w * 0.5f, h * 0.8f)
                            )
                        }
                        "Chrono" -> {
                            drawCircle(
                                color = Color(0xFF1A237E).copy(alpha = 0.25f),
                                radius = 230f,
                                center = Offset(w * 0.5f, h * 0.65f)
                            )
                            drawCircle(
                                color = Color(0xFF2979FF).copy(alpha = 0.55f),
                                radius = 230f,
                                center = Offset(w * 0.5f, h * 0.65f),
                                style = Stroke(width = 4f)
                            )
                            drawCircle(
                                color = Color(0xFF2979FF).copy(alpha = 0.25f),
                                radius = 238f + sin(simulationFrame.value * 0.15f) * 6f,
                                center = Offset(w * 0.5f, h * 0.65f),
                                style = Stroke(width = 1.5f)
                            )
                        }
                        "Kelly" -> {
                            val pulseAlpha = (sin(simulationFrame.value * 0.25f) + 1f) / 2f * 0.35f
                            drawRect(
                                color = Color(0xFFFF5252).copy(alpha = pulseAlpha),
                                style = Stroke(width = 24f)
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

                    // MUZZLE COMPRESSION BLAST EXHAUST FLASHLIGHT ON ACTIVE SHOOTING (CUSTOM MULTIPLAYER SKIN COLORS!)
                    if (isFiring) {
                        val blastPulse = Random.nextFloat() * 25f + 35f
                        val wpKey = when (selectedWeapon) {
                            WeaponType.M4A1 -> "M4A1"
                            WeaponType.AWP -> "AWP"
                            WeaponType.DEAGLE -> "DEAGLE"
                        }
                        val equippedSkin = equippedWeaponSkinsByWp[wpKey] ?: "Classic Slate"

                        val (primaryColor, secondaryColor) = when {
                            equippedSkin.contains("Viper") -> Color(0xFF39FF14) to Color(0xFF00E5FF) // Toxic Neon Green/Cyan
                            equippedSkin.contains("Drake") -> Color(0xFFFF1744) to Color(0xFFFF9100) // Flame Red/Orange Dragon
                            equippedSkin.contains("Shahi") -> Color(0xFFFFD700) to Color(0xFFFF8C00) // Lahore Luxury Imperial Gold
                            else -> Color(0xFFFFFFB3) to Color(0xFFFFCC00) // Standard bullet yellow
                        }

                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(primaryColor, secondaryColor.copy(alpha = 0.60f), Color.Transparent),
                                center = Offset(originX - 140f, originY - 80f),
                                radius = blastPulse
                            ),
                            radius = blastPulse,
                            center = Offset(originX - 140f, originY - 80f)
                        )

                        // Draw a high fidelity tracer beam line from barrel center direct to crosshair
                        drawLine(
                            color = primaryColor.copy(alpha = 0.70f),
                            start = Offset(originX - 140f, originY - 80f),
                            end = Offset(crosshairX, crosshairY),
                            strokeWidth = 3f
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

                // Precise tiny center dot scaled with settings redDotSizeVal
                drawCircle(Color.Red, radius = redDotSizeVal / 6f, center = Offset(crosshairX, crosshairY))
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

                // BATTLE ROYALE SIMULATION REMAINING ALIVE & KILLS WIDGET
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5722)),
                    modifier = Modifier.testTag("br_survivors_hud")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤 ALIVE ", color = Color(0xFFFFCC00), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("$survivorsCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.2f)))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💀 KILLS ", color = Color(0xFFFF3333), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("$arenaKills", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
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

            // GARENA FREE FIRE HP AND EP HUD BARS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // EP Bar (Yellow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EP  $playerEp / 100",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFB300)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(5.dp)
                                .background(Color(0xFF333333), RoundedCornerShape(2.5.dp))
                        ) {
                            val epPct = (playerEp / 100f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(epPct)
                                    .background(Color(0xFFFFD54F), RoundedCornerShape(2.5.dp))
                            )
                        }
                    }

                    // HP Bar (Light-blue / White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HP  $playerHp / 200",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E5FF)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(8.dp)
                                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                        ) {
                            val hpPct = (playerHp / 200f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(hpPct)
                                    .background(
                                        if (playerHp < 60) Color(0xFFD32F2F) else Color(0xFF00E5FF), 
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }

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
                    // TACTICAL JOYSTICK COMPONENT (SCALES DYNAMICALLY VIA SETTINGS HUD SLIDERS)
                    Box(
                        modifier = Modifier
                            .size((52 * joystickScaleVal).dp)
                            .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                            .border(1.5.dp, DeadShotTheme.LaserGreen.copy(alpha = 0.5f), CircleShape)
                            .testTag("joystick_pad_move"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((24 * joystickScaleVal).dp)
                                .background(DeadShotTheme.LaserGreen.copy(alpha = 0.8f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // ACTIVE CHARACTER SKILL TRIGGER BUTTON (FREE FIRE STYLE HERO POWER)
                    Button(
                        onClick = {
                            val statusMsg = viewModel.triggerCharacterSkill()
                            if (statusMsg.isNotEmpty()) {
                                combatFeedbackMessage = statusMsg
                                coroutineScope.launch {
                                    delay(2400)
                                    if (combatFeedbackMessage == statusMsg) {
                                        combatFeedbackMessage = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSkillActive) DeadShotTheme.GoldAccent else Color.Black.copy(alpha = 0.80f),
                            disabledContainerColor = Color(0xFF1F1F1F)
                        ),
                        border = BorderStroke(1.5.dp, if (skillCooldown > 0) Color.DarkGray else DeadShotTheme.GoldAccent),
                        shape = CircleShape,
                        enabled = skillCooldown == 0,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_character_skill_active")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (equippedActiveSkill) {
                                    "Drop the Beat" -> "🎵"
                                    "Time Turner" -> "🛡️"
                                    "Deadly Velocity" -> "⚡"
                                    "Master of All" -> "☸️"
                                    else -> "⭐"
                                },
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (skillCooldown > 0) "${skillCooldown}s" else if (isSkillActive) "ON" else "SKILL",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Black,
                                color = if (skillCooldown > 0) Color.Gray else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // GARENA FREE FIRE GLOO WALL DEPLOY BUTTON
                    Button(
                        onClick = {
                            val deployed = viewModel.deployGlooWall(crosshairX, crosshairY)
                            if (deployed) {
                                val statusMsg = "🛡️ FREEZING GLIP SHIELD DEPLOYED! INCOMING RIVAL IMPACT BLOCKED!"
                                combatFeedbackMessage = statusMsg
                                coroutineScope.launch {
                                    delay(2000)
                                    if (combatFeedbackMessage == statusMsg) {
                                        combatFeedbackMessage = ""
                                    }
                                }
                            } else if (glooWallCount == 0) {
                                val statusMsg = "❌ OUT OF GLOO STORES! WAIT FOR SUPPLY AIRDROP LOOT!"
                                combatFeedbackMessage = statusMsg
                                coroutineScope.launch {
                                    delay(2000)
                                    if (combatFeedbackMessage == statusMsg) {
                                        combatFeedbackMessage = ""
                                    }
                                }
                            } else if (isGlooWallDeployed) {
                                val statusMsg = "⚠️ GLOO WALL ALREADY ENGAGED IN THE SECTOR!"
                                combatFeedbackMessage = statusMsg
                                coroutineScope.launch {
                                    delay(2000)
                                    if (combatFeedbackMessage == statusMsg) {
                                        combatFeedbackMessage = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGlooWallDeployed) Color(0xFF00E5FF).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_gloo_wall_deploy")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "❄️",
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "GLOO ($glooWallCount)",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGlooWallDeployed) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // GARENA FREE FIRE MEDKIT BUTTON
                    Button(
                        onClick = {
                            if (playerHp >= 200) {
                                val statusMsg = "⚠️ HP IS ALREADY MAX - MEDKIT NOT REQUIRED!"
                                combatFeedbackMessage = statusMsg
                                coroutineScope.launch {
                                    delay(2000)
                                    if (combatFeedbackMessage == statusMsg) {
                                        combatFeedbackMessage = ""
                                    }
                                }
                            } else {
                                val used = viewModel.useMedkit()
                                if (!used && medkitCount == 0) {
                                    val statusMsg = "❌ OUT OF MEDKITS! SCAN THE AREA FOR SUPPLY CRATES!"
                                    combatFeedbackMessage = statusMsg
                                    coroutineScope.launch {
                                        delay(2000)
                                        if (combatFeedbackMessage == statusMsg) {
                                            combatFeedbackMessage = ""
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isChannelingHeal) Color(0xFF4CAF50).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF4CAF50)),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_medkit_heal")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🩹",
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "KIT ($medkitCount)",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChannelingHeal) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                        .size((64 * fireButtonScaleVal).dp)
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

        // AUTOMATED BATTLE ROYALE MULTIPLAYER MATCHEMAKER LIVE ACTION KILL FEED
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 16.dp, y = (-20).dp)
                .width(170.dp)
                .height(95.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = "⚔️ BATTLEGROUND ACTION FEED",
                    color = DeadShotTheme.GoldAccent,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(battleLogFeed.takeLast(4).reversed()) { log ->
                        val isMe = log.contains(profileName) || log.contains("YOU")
                        Text(
                            text = log,
                            color = if (isMe) Color(0xFFFF5722) else Color.White.copy(alpha = 0.85f),
                            fontSize = 7.sp,
                            maxLines = 1,
                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.5.dp)
                        )
                    }
                }
            }
        }

        // GARENA FREE FIRE MEDKIT HEAL CHANNELING PROGRESS BAR
        if (isChannelingHeal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .offset(y = 120.dp) // display below the crosshair
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🩹 APPLYING MEDKIT TREATMENT...",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    
                    // Progress Bar container
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                            .border(0.5.dp, Color(0xFF4CAF50), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(healProgress)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // SPECTACULAR FLASHING BOOYAH! VICTORY SCREEN OVERLAY
        if (isBooyahActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .pointerInput(Unit) {}, // block touch leak events
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                        .border(3.dp, DeadShotTheme.GoldAccent, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 54.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "CHEES!",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = DeadShotTheme.GoldAccent,
                        letterSpacing = 4.sp,
                        modifier = Modifier.testTag("booyah_victory_title")
                    )

                    Text(
                        text = "BATTLEGROUND CHAMPION #1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    Text(
                        text = "You successfully defended the frontier, cleared the Karachi Port sector, and neutralized all opponent hostiles!",
                        fontSize = 9.5.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Historic stats breakdown cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL KILLS", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("$arenaKills", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACCURACY CORE", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("${(100f * (score.toFloat().coerceAtLeast(1f) / (score.toFloat() + 50f))).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DSG COINS", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("+1,000 DSG", fontSize = 16.sp, fontWeight = FontWeight.Black, color = DeadShotTheme.GoldAccent, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.navigateTo(ActiveScreen.LOBBY)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeadShotTheme.GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_claim_booyah_lobby")
                    ) {
                        Text(
                            text = "CLAIM REWARDS & RETREAT TO LOBBY",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
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
