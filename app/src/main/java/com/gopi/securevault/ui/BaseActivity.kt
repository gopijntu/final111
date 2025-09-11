package com.gopi.securevault.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.gopi.securevault.ui.auth.LoginActivity

open class BaseActivity : AppCompatActivity() {

    private val logoutHandler = Handler(Looper.getMainLooper())
    private val TIMEOUT: Long = 2 * 60 * 1000 // 2 minutes

    private val logoutRunnable = Runnable {
        logoutUser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The original file had a commented out section for FLAG_SECURE,
        // and a line to clear it. I will keep the clearFlags line as it was the
        // last active state.
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onResume() {
        super.onResume()
        resetLogoutTimer()
    }

    override fun onPause() {
        super.onPause()
        logoutHandler.removeCallbacks(logoutRunnable)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetLogoutTimer()
    }

    private fun resetLogoutTimer() {
        logoutHandler.removeCallbacks(logoutRunnable)
        logoutHandler.postDelayed(logoutRunnable, TIMEOUT)
    }

    private fun logoutUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        logoutHandler.removeCallbacks(logoutRunnable)
    }
}
