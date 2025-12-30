package com.kufay.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kufay.app.ui.viewmodels.PinViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.kufay.app.R

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    onAuthenticated: () -> Unit
) {
    val pin by viewModel.pin.collectAsState()
    val pinState by viewModel.pinState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isChangingPin by viewModel.isChangingPin.collectAsState() // ✨ OBSERVER isChangingPin
    val haptic = LocalHapticFeedback.current

    // Get user's theme color
    val primaryColor = MaterialTheme.colorScheme.primary

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    var shakeError by remember { mutableStateOf(false) }
    var successAnimation by remember { mutableStateOf(false) }

    // Trigger entrance animation
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Handle error shake
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            shakeError = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(500)
            shakeError = false
        }
    }

    // ✨ MODIFIÉ : Handle success avec showText basé sur isChangingPin
    if (pinState is PinViewModel.PinState.Authenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF7)),
            contentAlignment = Alignment.Center
        ) {
            SuccessSplashScreen(
                primaryColor = primaryColor,
                showText = isChangingPin, // ✨ Passe la valeur observable
                onComplete = onAuthenticated
            )
        }
        return
    }

    // Warm background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Title with emoji
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(600)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔐",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = when (pinState) {
                            is PinViewModel.PinState.Setup -> "Créez votre code secret"
                            is PinViewModel.PinState.Confirm -> "Confirmez votre code"
                            is PinViewModel.PinState.Login -> "Entrez votre code"
                            else -> "Code secret"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2A2A2A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (pinState) {
                            is PinViewModel.PinState.Setup -> "4 chiffres pour protéger vos données"
                            is PinViewModel.PinState.Confirm -> "Tapez le même code pour confirmer"
                            is PinViewModel.PinState.Login -> "Votre code à 4 chiffres"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Middle section - PIN dots and messages
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "😅",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(800, delayMillis = 300)) +
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(800, delayMillis = 300)
                            )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        for (i in 0 until 4) {
                            PinDot(
                                isFilled = i < pin.length,
                                isError = shakeError,
                                isSuccess = successAnimation,
                                primaryColor = primaryColor,
                                index = i
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = successAnimation,
                    enter = fadeIn() + scaleIn()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Parfait !",
                            style = MaterialTheme.typography.titleLarge,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom section - Keypad
            AnimatedVisibility(
                visible = isVisible && !successAnimation,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800, delayMillis = 400)
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val keypadButtons = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9'),
                        listOf(' ', '0', '<')
                    )

                    keypadButtons.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { digit ->
                                if (digit == ' ') {
                                    Spacer(modifier = Modifier.size(64.dp))
                                } else {
                                    KeypadButton(
                                        digit = digit,
                                        primaryColor = primaryColor,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            when (digit) {
                                                '<' -> viewModel.deleteDigit()
                                                else -> viewModel.appendDigit(digit)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    LaunchedEffect(pin.length) {
                        if (pin.length == 4) {
                            delay(200)
                            viewModel.checkPin()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    isSuccess: Boolean,
    primaryColor: Color,
    index: Int
) {
    val offsetX by animateFloatAsState(
        targetValue = if (isError) {
            when (index % 2) {
                0 -> if (index == 0 || index == 2) -8f else 8f
                else -> if (index == 1) 8f else -8f
            }
        } else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "shake"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isSuccess -> 1.3f
            isFilled -> 1.1f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp)
            .scale(scale)
            .size(20.dp)
            .clip(CircleShape)
            .background(
                if (isFilled) {
                    if (isSuccess) Color(0xFF4CAF50) else primaryColor
                } else {
                    primaryColor.copy(alpha = 0.2f)
                }
            )
    )
}

@Composable
private fun KeypadButton(
    digit: Char,
    primaryColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )

    Surface(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        shape = CircleShape,
        color = if (digit == '<') {
            Color.Transparent
        } else {
            primaryColor.copy(alpha = 0.1f)
        },
        tonalElevation = if (digit == '<') 0.dp else 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (digit) {
                '<' -> {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Effacer",
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Text(
                        text = digit.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2A2A2A)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun SuccessSplashScreen(
    primaryColor: Color,
    showText: Boolean = false,
    onComplete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var splashVisible by remember { mutableStateOf(false) }
    var checkVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }

    val splashScale by animateFloatAsState(
        targetValue = if (splashVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splash_scale"
    )

    val splashRotation by animateFloatAsState(
        targetValue = if (splashVisible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splash_rotation"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (checkVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessHigh
        ),
        label = "check_scale"
    )

    val checkRotation by animateFloatAsState(
        targetValue = if (checkVisible) 0f else 90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "check_rotation"
    )

    val textOffset by animateFloatAsState(
        targetValue = if (textVisible && showText) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "text_offset"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible && showText) 1f else 0f,
        animationSpec = tween(300),
        label = "text_alpha"
    )

    var fadeOut by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fade"
    )

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        delay(50)
        splashVisible = true

        delay(200)
        checkVisible = true

        if (showText) {
            delay(150)
            textVisible = true
            delay(1100)
        } else {
            delay(600)
        }

        fadeOut = true
        delay(400)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pinok),
                    contentDescription = "Success splash",
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(splashScale)
                        .graphicsLayer {
                            rotationZ = splashRotation
                        },
                    contentScale = ContentScale.Fit
                )

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(checkScale)
                        .graphicsLayer {
                            rotationZ = checkRotation
                        }
                )
            }

            if (showText) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = textOffset.dp)
                        .graphicsLayer { this.alpha = textAlpha },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ Parfait !",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Nouveau code PIN enregistré",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}