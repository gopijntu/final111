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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        suspend fun migrateToNewPassword(context: Context, oldHash: String, newHash: String) = withContext(Dispatchers.IO) {
            // --- Step 1: Read all data from the old database into memory ---
            val oldFactory = SupportFactory(SQLiteDatabase.getBytes(oldHash.toCharArray()))
            val oldDb = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(oldFactory)
                .build()

            val aadharList = oldDb.aadharDao().getAll()
            val bankList = oldDb.bankDao().getAll()
            val cardList = oldDb.cardDao().getAll()
            val licenseList = oldDb.licenseDao().getAll()
            val panList = oldDb.panDao().getAll()
            val policyList = oldDb.policyDao().getAll()
            val voterIdList = oldDb.voterIdDao().getAll()

            oldDb.close()

            // --- Step 2: Create a new temp database and write the data into it ---
            val tempDbName = "securevault_temp.db"
            val tempDbFile = context.getDatabasePath(tempDbName)
            if (tempDbFile.exists()) {
                tempDbFile.delete()
            }

            val newFactory = SupportFactory(SQLiteDatabase.getBytes(newHash.toCharArray()))
            val newDb = Room.databaseBuilder(context, AppDatabase::class.java, tempDbName)
                .openHelperFactory(newFactory)
                .build()

            newDb.aadharDao().insertAll(aadharList)
            newDb.bankDao().insertAll(bankList)
            newDb.cardDao().insertAll(cardList)
            newDb.licenseDao().insertAll(licenseList)
            newDb.panDao().insertAll(panList)
            newDb.policyDao().insertAll(policyList)
            newDb.voterIdDao().insertAll(voterIdList)

            newDb.close()

            // --- Step 3: Replace the original DB with the new temp DB ---
            val originalDbFile = context.getDatabasePath(DATABASE_NAME)
            // To be safe, delete the original db's journal files if they exist
            context.getDatabasePath("$DATABASE_NAME-shm").delete()
            context.getDatabasePath("$DATABASE_NAME-wal").delete()

            tempDbFile.copyTo(originalDbFile, overwrite = true)
            tempDbFile.delete()
        }
    }
}
