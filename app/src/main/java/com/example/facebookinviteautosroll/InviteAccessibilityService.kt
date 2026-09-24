package com.example.facebookinviteautosroll

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*

class InviteAccessibilityService: AccessibilityService() {
    companion object {
        @Volatile var isRunning=false
        private const val FACEBOOK_PACKAGE="com.facebook.katana"
    }
    private var running=false
    private var invites=0
    private var scrolls=0
    private var maxInvites=50
    private var delayMs=3000L
    private var lastAction=0L
    private lateinit var wm:WindowManager
    private var overlay:LinearLayout?=null
    private lateinit var status:TextView

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning=true
        showOverlay()
    }
    override fun onDestroy() {
        isRunning=false
        running=false
        overlay?.let { runCatching { wm.removeView(it) } }
        overlay=null
        super.onDestroy()
    }
    override fun onInterrupt(){ running=false; update() }

    override fun onAccessibilityEvent(event:AccessibilityEvent?) {
        if(!running || event?.packageName?.toString()!=FACEBOOK_PACKAGE) return
        if(System.currentTimeMillis()-lastAction < delayMs) return
        val root=rootInActiveWindow ?: return
        val node=findExactInvite(root)
        if(node!=null && clickCandidate(node)) {
            lastAction=System.currentTimeMillis()
            if(node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                invites++
                update()
                if(invites>=maxInvites) stop()
                return
            }
        }
        if(System.currentTimeMillis()-lastAction>=delayMs) {
            if(root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                scrolls++
                lastAction=System.currentTimeMillis()
                update()
            }
        }
    }

    private fun findExactInvite(root:AccessibilityNodeInfo):AccessibilityNodeInfo? {
        root.findAccessibilityNodeInfosByText("Invite").forEach { n ->
            val t=n.text?.toString()?.trim() ?: return@forEach
            if(t.equals("Invite",true) && n.isEnabled) return n
        }
        return null
    }

    private fun clickCandidate(n:AccessibilityNodeInfo):Boolean {
        if(n.isClickable) return true
        var p=n.parent
        repeat(3) {
            if(p?.isClickable==true && p.isEnabled) return true
            p=p?.parent
        }
        return false
    }

    private fun start() {
        val p=getSharedPreferences("settings",MODE_PRIVATE)
        maxInvites=p.getInt("max",50)
        delayMs=(p.getFloat("delay",3f)*1000).toLong()
        invites=0
        scrolls=0
        lastAction=0
        running=true
        update()
    }

    private fun stop(){ running=false; update() }

    private fun showOverlay() {
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        val box=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(22,14,16,14)
            background=GradientDrawable().apply{
                setColor(Color.WHITE)
                cornerRadius=22f
            }
            elevation=14f
        }
        status=TextView(this).apply{
            setTextColor(Color.DKGRAY)
            textSize=14f
        }
        box.addView(status)
        box.addView(Button(this).apply{
            text="START"
            setOnClickListener{start()}
        })
        box.addView(Button(this).apply{
            text="■ STOP"
            setTextColor(Color.WHITE)
            background=GradientDrawable().apply{
                setColor(Color.rgb(255,23,79))
                cornerRadius=48f
            }
            setOnClickListener{stop()}
        })
        overlay=box
        val lp=WindowManager.LayoutParams(
            360,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply{
            gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y=120
        }
        wm.addView(box,lp)
        update()
    }

    private fun update(){
        if(!::status.isInitialized)return
        val state=if(running) "● RUNNING" else "● STOPPED"
        status.text="$state\nInvites: $invites/$maxInvites | Scrolls: $scrolls"
    }
}
