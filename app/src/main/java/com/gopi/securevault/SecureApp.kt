package com.gopi.securevault

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.gopi.securevault.ui.auth.LoginActivity
import net.sqlcipher.database.SQLiteDatabase

class SecureApp : Application() {

    private lateinit var logoutHandler: Handler
    private lateinit var logoutRunnable: Runnable
    private val TIMEOUT: Long = 2 * 60 * 1000 // 2 minutes

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize SQLCipher once
        SQLiteDatabase.loadLibs(this)

        // Suppress SQLCipher verbose warnings
        System.setProperty("net.sqlcipher.database.VERBOSE", "false")

        logoutHandler = Handler(Looper.getMainLooper())
        logoutRunnable = Runnable {
            logoutUser()
        }
    }

    fun resetSessionTimer() {
        logoutHandler.removeCallbacks(logoutRunnable)
        logoutHandler.postDelayed(logoutRunnable, TIMEOUT)
    }

    fun startSessionTimer() {
        resetSessionTimer()
    }

    fun stopSessionTimer() {
        logoutHandler.removeCallbacks(logoutRunnable)
    }

    private fun logoutUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        lateinit var instance: SecureApp
            private set
    }
}
