package com.progress.passwordkeeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.progress.passwordkeeper.data.PasswordEntry
import com.progress.passwordkeeper.vievmodel.PasswordViewModel

@Composable
fun PasswordListScreen(viewModel: PasswordViewModel) {
    val passwords by viewModel.passwords.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить",
                    tint = Color.hsl(283f, 0.44f, 0.25f),
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color(0x44E0E0E0), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Text(
                    text = "Password Keeper",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4A235A),
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = { /* TODO: смена ПИН */ },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Сменить ПИН",
                        tint = Color(0xFF4A235A)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passwords, key = { it.id }) { entry ->
                    PasswordItem(
                        entry = entry,
                        onDelete = { viewModel.deletePassword(entry.id) },
                        onUpdate = { updatedEntry -> viewModel.updatePassword(updatedEntry) }
                    )
                }
            }
        }

        if (showDialog) {
            AddPasswordDialog(
                onDismiss = { showDialog = false },
                onSave = { app, login, pass ->
                    viewModel.addPassword(app, login, pass)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun PasswordItem(
    entry: PasswordEntry,
    onDelete: () -> Unit,
    onUpdate: (PasswordEntry) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.background(Color(0x024A235A))) {
            // Хэдер
            Text(
                text = entry.appName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x444A235A))
                    .padding(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Логин: ${entry.login}",
                color = Color.Black,
                modifier = Modifier
                    .padding(start = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Пароль: ")
                    Text(
                        text = if (passwordVisible) entry.password else "••••••••••••••••",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = {
                        if (!passwordVisible) { // только при попытке показать
                            activity?.let { act ->
                                FingerprintLogin(
                                    activity = act,
                                    onSuccess = { passwordVisible = true },
                                    onError = { error -> println("Ошибка биометрии: $error") }
                                )
                            }
                        } else {
                            passwordVisible = false
                        }
                    }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Скрыть" else "Показать"
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showUpdateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = Color.hsl(283f, 0.44f, 0.25f),
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.hsl(0f, 1f, 0.25f),
                        )
                    }
                }
            }
        }
    }

    // Диалог удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Вы действительно хотите удалить данные для ${entry.appName}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Удалить", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог редактирования
    if (showUpdateDialog) {
        UpdatePasswordDialog(
            entry = entry,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { updated ->
                onUpdate(updated)
                showUpdateDialog = false
            }
        )
    }
}

@Composable
fun AddPasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp), // <- кастомный радиус
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Добавить аккаунт", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Название приложения") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(2.dp))
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(2.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    TextButton(onClick = {
                        if (appName.isNotBlank() && login.isNotBlank() && password.isNotBlank()) {
                            onSave(appName, login, password)
                        }
                    }) { Text("Сохранить") }
                }
            }
        }
    }
}


@Composable
fun UpdatePasswordDialog(
    entry: PasswordEntry,
    onDismiss: () -> Unit,
    onUpdate: (PasswordEntry) -> Unit
) {
    var appName by remember { mutableStateOf(entry.appName) }
    var login by remember { mutableStateOf(entry.login) }
    var password by remember { mutableStateOf(entry.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать аккаунт") },
        text = {
            Column {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Название приложения") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onUpdate(
                    entry.copy(appName = appName, login = login, password = password)
                )
            }) {
                Text("Обновить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}