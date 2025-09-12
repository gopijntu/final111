package com.gopi.securevault.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.gopi.securevault.util.SessionManager

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Prevent screenshots & screen recording
        // window.setFlags(
        //     WindowManager.LayoutParams.FLAG_SECURE,
        //     WindowManager.LayoutParams.FLAG_SECURE
        // )
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        SessionManager.resetTimer()
    }
}
