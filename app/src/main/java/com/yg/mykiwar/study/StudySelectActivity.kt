package com.yg.mykiwar.study

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.jakewharton.rxbinding3.view.clicks
import com.yg.mykiwar.R
import com.yg.mykiwar.util.presenter.DictSelectPresenter
import com.yg.mykiwar.util.renderer.BackgroundRenderer
import com.yg.mykiwar.util.renderer.ObjectRenderer
import com.yg.mykiwar.util.renderer.PlaneRenderer
import com.yg.mykiwar.util.renderer.PointCloudRenderer
import kotlinx.android.synthetic.main.activity_study_select.*

class StudySelectActivity : AppCompatActivity() {

    private lateinit var dictSelectPresenter : DictSelectPresenter

    private val backgroundRenderer = BackgroundRenderer()
    private val virtualObject = ObjectRenderer()
    private val virtualObjectShadow = ObjectRenderer()
    private val planeRenderer = PlaneRenderer()
    private val pointCloudRenderer = PointCloudRenderer()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(R.color.background_tab_pressed)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_select)
        initPresenter()
        setOnBinding()
    }

    private fun initPresenter(){
        dictSelectPresenter = DictSelectPresenter()
        dictSelectPresenter.view = this
    }

    private fun initView(){

    }

    private fun setOnBinding(){
        btn_dict_sel_animal.clicks().subscribe{dictSelectPresenter.selectTheme(0)}
        btn_dict_sel_plant.clicks().subscribe{dictSelectPresenter.selectTheme(1)}
        btn_dict_sel_bug.clicks().subscribe{dictSelectPresenter.selectTheme(2)}
        btn_dict_sel_dinasour.clicks().subscribe{dictSelectPresenter.selectTheme(3)}

    }

    fun selectTheme(theme : Int){
        val themeIntent = Intent(this, StudyAlignActivity::class.java)
        //themeIntent.extras.putInt("theme", 0)
        startActivity(themeIntent)
    }
}
