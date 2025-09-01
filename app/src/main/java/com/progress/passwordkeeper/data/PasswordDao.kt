package com.progress.passwordkeeper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PasswordDao {
    @Insert
    suspend fun insert(entry: PasswordEntry)

    @Query("SELECT * FROM passwords")
    suspend fun getAll(): List<PasswordEntry>
}