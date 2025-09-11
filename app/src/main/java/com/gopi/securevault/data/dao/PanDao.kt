package com.gopi.securevault.data.dao

import androidx.room.*
import com.gopi.securevault.data.entities.PanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PanDao {
    @Query("SELECT * FROM pan")
    fun observeAll(): Flow<List<PanEntity>>

    @Query("SELECT * FROM pan")
    suspend fun getAll(): List<PanEntity>

    @Insert
    suspend fun insert(entity: PanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PanEntity>)

    @Update
    suspend fun update(entity: PanEntity)

    @Delete
    suspend fun delete(entity: PanEntity)
}
