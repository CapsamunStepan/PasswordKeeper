package com.progress.passwordkeeper.vievmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.progress.passwordkeeper.data.AppDatabase
import com.progress.passwordkeeper.data.PasswordEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "passwords.db"
    ).build()

    private val dao = db.passwordDao()

    private val _passwords = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val passwords = _passwords.asStateFlow()

    init {
        viewModelScope.launch {
            _passwords.value = dao.getAll()
        }
    }

    fun addPassword(appName: String, login: String, password: String) {
        viewModelScope.launch {
            dao.insert(PasswordEntry(appName = appName, login = login, password = password))
            _passwords.value = dao.getAll()
        }
    }
}