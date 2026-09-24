package com.example.facebookinviteautosroll

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(28,28,28,28)
            setBackgroundColor(0xFFF5F7FB.toInt())
        }
        val title=TextView(this).apply {
            text="Facebook Invite\nAuto Scroll"
            textSize=28f
            setTextColor(0xFF172033.toInt())
            setTypeface(typeface,android.graphics.Typeface.BOLD)
        }
        root.addView(title)
        root.addView(TextView(this).apply {
            text="Kotlin AccessibilityService • Exact Matching • Floating Controls"
            textSize=14f
            setTextColor(0xFF687386.toInt())
            setPadding(0,8,0,20)
        })

        val max=EditText(this).apply {
            hint="Maximum invites"
            inputType=2
            setText(prefs.getInt("max",50).toString())
        }
        root.addView(max)

        val delay=EditText(this).apply {
            hint="Delay in seconds"
            inputType=8194
            setText(prefs.getFloat("delay",3f).toString())
        }
        root.addView(delay)

        root.addView(Button(this).apply {
            text="SAVE SETTINGS"
            setOnClickListener {
                prefs.edit()
                    .putInt("max",max.text.toString().toIntOrNull()?.coerceIn(1,500) ?: 50)
                    .putFloat("delay",delay.text.toString().toFloatOrNull()?.coerceIn(1f,30f) ?: 3f)
                    .apply()
                status.text="Settings saved"
            }
        })

        status=TextView(this).apply {
            textSize=16f
            setPadding(0,24,0,12)
            setTextColor(0xFF172033.toInt())
        }
        root.addView(status)

        root.addView(Button(this).apply {
            text="OPEN ACCESSIBILITY SETTINGS"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        root.addView(TextView(this).apply {
            text="Only visible accessibility actions are used. No anti-detection or stealth logic is included."
            setTextColor(0xFF687386.toInt())
            setPadding(0,24,0,0)
        })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        status.text=if (InviteAccessibilityService.isRunning)
            "Accessibility service: ON"
        else "Accessibility service: OFF — enable it above"
    }
}
