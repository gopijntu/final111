package com.gopi.securevault.backup

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.room.Room
import com.gopi.securevault.data.db.AppDatabase
import com.gopi.securevault.util.AESUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context) {

    private val db by lazy { AppDatabase.get(context) }

    suspend fun backupDatabase(destinationUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                AppDatabase.closeInstance()
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup successful!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                AppDatabase.get(context)
            }
        }
    }

    suspend fun restoreDatabase(password: String, sourceUri: Uri, onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            val tempBackupFile = File(context.cacheDir, "restore_temp.db")
            val tempBackupDbName = "restore_temp.db"

            try {
                // 1. Copy backup to a temporary file
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(tempBackupFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // 2. Try to open the temporary database with the provided password's hash
                val newSalt = com.gopi.securevault.util.PasswordUtils.generateSalt()
                val newHash = com.gopi.securevault.util.PasswordUtils.hashWithSalt(password, newSalt)
                var isValid = false
                try {
                    val factory = SupportFactory(SQLiteDatabase.getBytes(newHash.toCharArray()))
                    val tempDb = Room.databaseBuilder(context, AppDatabase::class.java, tempBackupDbName)
                        .openHelperFactory(factory)
                        .build()

                    tempDb.openHelper.writableDatabase // This will trigger the open and validation
                    tempDb.close()
                    isValid = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    isValid = false
                }

                // 3. If valid, proceed with restore
                if (isValid) {
                    AppDatabase.closeInstance()
                    val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                    tempBackupFile.copyTo(dbFile, overwrite = true)

                    // 4. CRITICAL: Update the app's master password hash to match the restored DB
                    val prefs = com.gopi.securevault.util.CryptoPrefs(context)
                    prefs.putString("salt", newSalt)
                    prefs.putString("master_hash", newHash)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Restore successful! Restarting app...", Toast.LENGTH_LONG).show()
                        onSuccess()
                    }
                } else {
                     withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Restore failed: Incorrect password or corrupt backup file.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (tempBackupFile.exists()) {
                    tempBackupFile.delete()
                }
                // Don't re-open the database here, let the app do it after restart
            }
        }
    }

    suspend fun backupToJson(password: String, destinationUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val backupData = BackupData(
                    aadhar = db.aadharDao().getAll(),
                    banks = db.bankDao().getAll(),
                    cards = db.cardDao().getAll(),
                    policies = db.policyDao().getAll(),
                    pan = db.panDao().getAll(),
                    voterId = db.voterIdDao().getAll(),
                    license = db.licenseDao().getAll()
                )
                val json = Gson().toJson(backupData)
                val encryptedJson = AESUtils.encrypt(json, password)

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    outputStream.write(encryptedJson!!.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup successful!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun restoreFromJson(password: String, sourceUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val encryptedJson = context.contentResolver.openInputStream(sourceUri)?.use {
                    it.bufferedReader().readText()
                } ?: throw Exception("Could not read from file")

                val json = AESUtils.decrypt(encryptedJson, password)
                if (json == null) {
                    throw Exception("Decryption failed. Incorrect password or corrupt file.")
                }
                val backupData = Gson().fromJson(json, BackupData::class.java)

                // This is a simple data import. It does not change the master password.
                // It happens on the currently open database.
                db.clearAllTablesManually()

                backupData.aadhar.forEach { db.aadharDao().insert(it) }
                backupData.banks.forEach { db.bankDao().insert(it) }
                backupData.cards.forEach { db.cardDao().insert(it) }
                backupData.policies.forEach { db.policyDao().insert(it) }
                backupData.pan.forEach { db.panDao().insert(it) }
                backupData.voterId.forEach { db.voterIdDao().insert(it) }
                backupData.license.forEach { db.licenseDao().insert(it) }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "JSON data restored successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getBackupFileName(isJson: Boolean): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return if (isJson) "securevault_backup_$timestamp.vaultbackup" else "securevault_backup_$timestamp.db"
    }
}
