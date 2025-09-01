package com.progress.passwordkeeper.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint


@Composable
fun PinScreen(
    pinLength: Int = 6,
    message: String,
    onPinComplete: (String) -> Unit,
    shakeOffset: Float = 0f
) {
    var pin by remember { mutableStateOf("") }
    val context: Context = LocalContext.current
    val activity = context as? FragmentActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset(x = shakeOffset.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = message,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        PinIndicator(pin = pin, pinLength = pinLength)

        Spacer(Modifier.height(32.dp))

        PinKeyboardStyled(
            onNumberClick = { number ->
                if (pin.length < pinLength) {
                    pin += number
                    if (pin.length == pinLength) {
                        onPinComplete(pin)
                        pin = ""
                    }
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                }
            },
            onFingerprintClick = {
                activity?.let { act ->
                    FingerprintLogin(
                        activity = act,
                        onSuccess = { onPinComplete("fingerprint") },
                        onError = { error -> println("Ошибка биометрии: $error") }
                    )
                }
            }
        )
    }
}

@Composable
fun PinIndicator(pin: String, pinLength: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        for (i in 0 until pinLength) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (i < pin.length) Color.Black else Color.Gray)
            )
        }
    }
}

@Composable
fun PinKeyboardStyled(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onFingerprintClick: () -> Unit
) {
    val keys = listOf(
        listOf("1","2","3"),
        listOf("4","5","6"),
        listOf("7","8","9"),
        listOf("finger", "0", "⌫")
    )

    rememberCoroutineScope() // <- корутинный скоуп

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    val pressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "")

                    when (key) {
                        "finger" -> { // 👈 кнопка отпечатка
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(scale)
                                    .background(Color(0xFFE0F7FA), shape = RoundedCornerShape(32.dp))
                                    .clickable {
                                        onFingerprintClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint",
                                    tint = Color(0xFF00796B),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        else -> {
                            val isDelete = key == "⌫"
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(scale)
                                    .background(
                                        color = if (isDelete) Color(0xAAFFD1DC) else Color(0xFFF1F1F1),
                                        shape = RoundedCornerShape(32.dp))
                                    .clickable {
                                        if (isDelete) onDeleteClick()
                                        else onNumberClick(key)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    key,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDelete) Color.Red else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FingerprintLogin(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) { onSuccess() }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("Authentication failed")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Вход по отпечатку")
        .setSubtitle("Используйте биометрию для входа")
        .setNegativeButtonText("Отмена")
        .build()

    biometricPrompt.authenticate(promptInfo)
}