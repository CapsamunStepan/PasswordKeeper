package com.progress.passwordkeeper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.progress.passwordkeeper.vievmodel.PasswordViewModel

@Composable
fun PasswordListScreen(viewModel: PasswordViewModel) {
    val passwords by viewModel.passwords.collectAsState(initial = emptyList())
    var appName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(passwords) { entry ->
                Text("${entry.appName} | ${entry.login} | ${entry.password}")
            }
        }
        OutlinedTextField(value = appName, onValueChange = { appName = it }, label = { Text("App Name") })
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Login") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Button(onClick = {
            viewModel.addPassword(appName, login, password)
            appName = ""; login = ""; password = ""
        }) {
            Text("Сохранить")
        }
    }
}
