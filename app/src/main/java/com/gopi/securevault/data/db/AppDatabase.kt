package com.gopi.securevault.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gopi.securevault.data.dao.*
import com.gopi.securevault.data.entities.*
import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase
import com.gopi.securevault.util.CryptoPrefs

@Database(
    entities = [BankEntity::class, CardEntity::class, PolicyEntity::class, AadharEntity::class, PanEntity::class, VoterIdEntity::class, LicenseEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun cardDao(): CardDao
    abstract fun policyDao(): PolicyDao
    abstract fun aadharDao(): AadharDao
    abstract fun panDao(): PanDao
    abstract fun voterIdDao(): VoterIdDao
    abstract fun licenseDao(): LicenseDao

    suspend fun clearAllTablesManually() {
        clearAllTables()
    }

    companion object {
        const val DATABASE_NAME = "securevault.db"
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    return instance
                }

                val prefs = CryptoPrefs(context)
                val passphrase: CharArray = (prefs.getString("master_hash", null) ?: "fallback-key").toCharArray()
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))

                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }

        fun closeInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }

        suspend fun migrateToNewPassword(context: Context, oldHash: String, newHash: String) {
            val tempDbName = "securevault_temp.db"
            val tempDbFile = context.getDatabasePath(tempDbName)
            if (tempDbFile.exists()) {
                tempDbFile.delete()
            }

            // 1. Create a new, empty database with the new password hash
            val newFactory = SupportFactory(SQLiteDatabase.getBytes(newHash.toCharArray()))
            val newDb = Room.databaseBuilder(context, AppDatabase::class.java, tempDbName)
                .openHelperFactory(newFactory)
                .setJournalMode(JournalMode.TRUNCATE) // Disable WAL for safe file copy
                .build()

            // 2. Open the old database with the old password hash
            val oldFactory = SupportFactory(SQLiteDatabase.getBytes(oldHash.toCharArray()))
            val oldDb = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(oldFactory)
                .setJournalMode(JournalMode.TRUNCATE) // Disable WAL for safe file copy
                .build()

            // 3. Copy data from old DB to new DB for all tables
            newDb.aadharDao().insertAll(oldDb.aadharDao().getAll())
            newDb.bankDao().insertAll(oldDb.bankDao().getAll())
            newDb.cardDao().insertAll(oldDb.cardDao().getAll())
            newDb.licenseDao().insertAll(oldDb.licenseDao().getAll())
            newDb.panDao().insertAll(oldDb.panDao().getAll())
            newDb.policyDao().insertAll(oldDb.policyDao().getAll())
            newDb.voterIdDao().insertAll(oldDb.voterIdDao().getAll())

            // 4. Close both database connections
            oldDb.close()
            newDb.close()

            // 5. Replace the old database file with the new one
            val originalDbFile = context.getDatabasePath(DATABASE_NAME)
            tempDbFile.copyTo(originalDbFile, overwrite = true)
            tempDbFile.delete()
        }
    }
}
