package com.yg.mykiwar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.yg.mykiwar.deco.DecoActivity
import com.yg.mykiwar.play.catchgame.CatchActivity
import com.yg.mykiwar.study.StudySelectActivity
import com.yg.mykiwar.util.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(R.color.background_tab_pressed)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PermissionHelper.hasCameraPermission(this))
            PermissionHelper.requestCameraPermission(this)
//        val mainIntent = Intent(this,
//                KotlinActivity::class.java)
//        mainIntent.putExtra("text", "text")
//        startActivity(mainIntent)



        //intent.extras["text"].toString()

//        button.setOnClickListener {
//            client.startRecording(true)
//        }

        btn_main_play.setOnClickListener {
            startActivity(Intent(this, CatchActivity::class.java))
        }

        btn_main_deco.setOnClickListener {
            startActivity(Intent(this, DecoActivity::class.java))
        }

        btn_main_dict.setOnClickListener {
        }

        btn_main_study.setOnClickListener {
            startActivity(Intent(this, StudySelectActivity::class.java))
        }
    }
}
