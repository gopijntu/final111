package com.gopi.securevault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gopi.securevault.data.db.AppDatabase
import com.gopi.securevault.databinding.ActivityChangePasswordBinding
import com.gopi.securevault.util.CryptoPrefs
import com.gopi.securevault.util.PasswordUtils
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var prefs: CryptoPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        prefs = CryptoPrefs(this)

        binding.btnSave.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!PasswordUtils.isPasswordValid(newPassword)) {
                Toast.makeText(this, "Password must be at least 8 characters with letters, numbers, and symbols.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val oldHash = prefs.getString("master_hash", null)
            val salt = prefs.getString("salt", null)

            if (oldHash == null || salt == null) {
                Toast.makeText(this, "Could not retrieve current password information.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (PasswordUtils.hashWithSalt(oldPassword, salt) == oldHash) {
                lifecycleScope.launch {
                    try {
                        val newSalt = PasswordUtils.generateSalt()
                        val newHash = PasswordUtils.hashWithSalt(newPassword, newSalt)

                        // Close the active instance before migration
                        AppDatabase.closeInstance()

                        // Perform the migration
                        AppDatabase.migrateToNewPassword(applicationContext, oldHash, newHash)

                        // If migration succeeds, update prefs
                        prefs.putString("salt", newSalt)
                        prefs.putString("master_hash", newHash)

                        Toast.makeText(this@ChangePasswordActivity, "Password changed successfully. Please log in again.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ChangePasswordActivity, "Failed to change password: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
