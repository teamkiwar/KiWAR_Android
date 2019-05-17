package com.yg.mykiwar.play

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.yg.mykiwar.R
import com.yg.mykiwar.play.catchgame.CatchActivity
import com.yg.mykiwar.play.findgame.FindActivity
import kotlinx.android.synthetic.main.activity_play_select.*

class PlaySelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_select)

        btn_select_find.setOnClickListener {
            startActivity(Intent(this, FindActivity::class.java))
        }

        btn_select_catch.setOnClickListener {
            startActivity(Intent(this, CatchActivity::class.java))
        }
    }
}
