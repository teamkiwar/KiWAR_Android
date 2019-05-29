package com.yg.mykiwar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.yg.mykiwar.deco.SizeCheckActivity
import com.yg.mykiwar.dict.DictActivity
import com.yg.mykiwar.play.PlaySelectActivity
import com.yg.mykiwar.study.StudyActivity
import com.yg.mykiwar.util.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PermissionHelper.hasCameraPermission(this))
            PermissionHelper.requestCameraPermission(this)

        if(!PermissionHelper.hasReadExternalStoragePermission(this))
            PermissionHelper.requestReadExternalStoragePermission(this)

        if(!PermissionHelper.hasWriteExternalStoragePermission(this))
            PermissionHelper.requestWriteExternalStoragePermission(this)



        btn_main_play.setOnClickListener {
            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_main_deco.setOnClickListener {
            startActivity(Intent(this, PlaySelectActivity::class.java))
        }

        btn_main_dict.setOnClickListener {
            startActivity(Intent(this, DictActivity::class.java))
        }

        btn_main_study.setOnClickListener {
            startActivity(Intent(this, StudyActivity::class.java))
        }
    }
}
