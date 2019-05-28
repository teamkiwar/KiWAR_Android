package com.yg.mykiwar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.yg.mykiwar.util.CommonData
import com.yg.mykiwar.util.SharedPreferenceController

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val handler = Handler()
        handler.postDelayed(Runnable {
            CommonData.dictList = ArrayList()
            CommonData.dictList = SharedPreferenceController.getDictList(this)
            startActivity(Intent(this, MainActivity::class.java))
        }, 3000)
    }
}
