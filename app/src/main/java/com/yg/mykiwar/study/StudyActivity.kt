package com.yg.mykiwar.study

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.activity_study.*

class StudyActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        btn_study_animal.setOnClickListener(this)
        btn_study_dinosaur.setOnClickListener(this)
        btn_study_insects.setOnClickListener(this)
        btn_study_plants.setOnClickListener(this)
        btn_study_back.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!){
            btn_study_animal->{
                startActivity(Intent(this, StudyCardActivity::class.java))
            }
            btn_study_back->{
                finish()
            }
            else->{
                Toast.makeText(this, "준비중입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
