package com.progress.passwordkeeper.data

import androidx.room.*

@Dao
interface PasswordDao {
    @Insert
    suspend fun insert(entry: PasswordEntry)

    @Query("SELECT * FROM passwords")
    suspend fun getAll(): List<PasswordEntry>

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(entry: PasswordEntry)
}