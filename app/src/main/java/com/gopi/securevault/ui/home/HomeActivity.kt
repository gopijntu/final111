package com.gopi.securevault.ui.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gopi.securevault.R
import com.gopi.securevault.databinding.ActivityHomeBinding
import com.gopi.securevault.ui.BaseActivity
import com.gopi.securevault.ui.aadhar.AadharActivity
import com.gopi.securevault.ui.auth.LoginActivity
import com.gopi.securevault.ui.banks.BanksActivity
import com.gopi.securevault.ui.cards.CardsActivity
import com.gopi.securevault.ui.license.LicenseActivity
import com.gopi.securevault.ui.pan.PanActivity
import com.gopi.securevault.ui.policies.PoliciesActivity
import com.gopi.securevault.ui.settings.SettingsActivity
import com.gopi.securevault.ui.voterid.VoterIdActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var menuCards: List<MaterialCardView>
    private val handler = Handler(Looper.getMainLooper())
    private var currentCardIndex = 0

    // Timing
    private val cardDelayDuration = 1000L     // Delay between glowing each card
    private val glowAnimationDuration = 800L  // Duration of glow animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize cards in proper sequence
        menuCards = listOf(
            binding.btnBanks,
            binding.btnCards,
            binding.btnPolicies,
            binding.btnAadhar,
            binding.btnPan,
            binding.btnLicense,
            binding.btnVoterId,
            binding.btnSettings
        )

        // Set up click listeners
        binding.btnBanks.setOnClickListener { startActivity(Intent(this, BanksActivity::class.java)) }
        binding.btnCards.setOnClickListener { startActivity(Intent(this, CardsActivity::class.java)) }
        binding.btnPolicies.setOnClickListener { startActivity(Intent(this, PoliciesActivity::class.java)) }
        binding.btnAadhar.setOnClickListener { startActivity(Intent(this, AadharActivity::class.java)) }
        binding.btnPan.setOnClickListener { startActivity(Intent(this, PanActivity::class.java)) }
        binding.btnLicense.setOnClickListener { startActivity(Intent(this, LicenseActivity::class.java)) }
        binding.btnVoterId.setOnClickListener { startActivity(Intent(this, VoterIdActivity::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        // Initialize all cards stroke to 0
        menuCards.forEach { it.strokeWidth = 0 }

        // Start the glow loop
        startGlowAnimation()
    }

    private fun startGlowAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (menuCards.isEmpty()) return

                // Turn off previous card glow
                val previousIndex = (currentCardIndex - 1 + menuCards.size) % menuCards.size
                animateStroke(menuCards[previousIndex], false)

                // Turn on current card glow
                val currentCard = menuCards[currentCardIndex]
                animateStroke(currentCard, true)

                // Move to next card
                currentCardIndex = (currentCardIndex + 1) % menuCards.size

                // Schedule next animation
                handler.postDelayed(this, cardDelayDuration)
            }
        }, cardDelayDuration)
    }

    private fun animateStroke(card: MaterialCardView, isGlowing: Boolean) {
        val startWidth = if (isGlowing) 0 else 8   // Increased width for stronger glow
        val endWidth = if (isGlowing) 8 else 0

        val startColor = if (isGlowing)
            ContextCompat.getColor(this, R.color.transparent_neon)
        else
            ContextCompat.getColor(this, R.color.glow_cyan)

        val endColor = if (isGlowing)
            ContextCompat.getColor(this, R.color.glow_cyan)
        else
            ContextCompat.getColor(this, R.color.transparent_neon)

        // Animate stroke width
        card.strokeWidth = endWidth

        // Animate stroke color
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnimator.duration = glowAnimationDuration
        colorAnimator.addUpdateListener { animator ->
            card.strokeColor = animator.animatedValue as Int
        }
        colorAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
