package com.gopi.securevault.data.dao

import androidx.room.*
import com.gopi.securevault.data.entities.PolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PolicyDao {
    @Query("SELECT * FROM policies ORDER BY id DESC")
    fun observeAll(): Flow<List<PolicyEntity>>

    @Query("SELECT * FROM policies")
    suspend fun getAll(): List<PolicyEntity>

    @Insert
    suspend fun insert(entity: PolicyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PolicyEntity>)

    @Update
    suspend fun update(entity: PolicyEntity)

    @Delete
    suspend fun delete(entity: PolicyEntity)
}
