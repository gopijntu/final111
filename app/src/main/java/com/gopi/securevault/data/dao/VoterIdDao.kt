package com.gopi.securevault.data.dao

import androidx.room.*
import com.gopi.securevault.data.entities.VoterIdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterIdDao {
    @Query("SELECT * FROM voter_id")
    fun observeAll(): Flow<List<VoterIdEntity>>

    @Query("SELECT * FROM voter_id")
    suspend fun getAll(): List<VoterIdEntity>

    @Insert
    suspend fun insert(entity: VoterIdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<VoterIdEntity>)

    @Update
    suspend fun update(entity: VoterIdEntity)

    @Delete
    suspend fun delete(entity: VoterIdEntity)
}
