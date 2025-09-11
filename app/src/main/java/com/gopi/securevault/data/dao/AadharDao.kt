package com.gopi.securevault.data.dao

import androidx.room.*
import com.gopi.securevault.data.entities.AadharEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AadharDao {
    @Query("SELECT * FROM aadhar ORDER BY id DESC")
    fun observeAll(): Flow<List<AadharEntity>>

    @Query("SELECT * FROM aadhar")
    suspend fun getAll(): List<AadharEntity>

    @Insert
    suspend fun insert(entity: AadharEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AadharEntity>)

    @Update
    suspend fun update(entity: AadharEntity)

    @Delete
    suspend fun delete(entity: AadharEntity)
}
