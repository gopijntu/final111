package com.gopi.securevault.util

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.gopi.securevault.ui.auth.LoginActivity

object SessionManager : DefaultLifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())
    private const val TIMEOUT: Long = 2 * 60 * 1000 // 2 minutes
    private var logoutRunnable: Runnable? = null

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        // App is in the foreground
        logoutRunnable = Runnable {
            logoutUser()
        }
        resetTimer()
    }

    override fun onStop(owner: LifecycleOwner) {
        // App is in the background
        stopTimer()
    }

    fun resetTimer() {
        stopTimer()
        logoutRunnable?.let { handler.postDelayed(it, TIMEOUT) }
    }

    private fun stopTimer() {
        logoutRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun logoutUser() {
        appContext?.let {
            val intent = Intent(it, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.startActivity(intent)
        }
    }
}
