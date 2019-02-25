package com.yg.mykiwar.study

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.align.StudyAlignAdapter
import com.yg.mykiwar.study.align.StudyAlignDatas
import com.yg.mykiwar.study.model.StudyModelSurfaceView
import com.yg.mykiwar.study.model.StudySceneLoader
import com.yg.mykiwar.util.presenter.StudyAlignPresenter
import kotlinx.android.synthetic.main.activity_study_align.*

class StudyAlignActivity : AppCompatActivity() {

    private val TAG = StudyAlignActivity::class.java.simpleName

    private var dicTheme : Int = 0
    private lateinit var studyAlignPresenter : StudyAlignPresenter
    private lateinit var studyAlignAdapter: StudyAlignAdapter
    private lateinit var requestManager : RequestManager
    //이 아래 temp

    /**
     * Type of model if file name has no extension (provided though content provider)
     */
    var paramType: Int = 0
    /**
     * The file to load. Passed as input parameter
     */
    var paramUri: Uri? = null
    /**
     * Enter into Android Immersive mode so the renderer is full screen or not
     */
    var immersiveMode = true
    /**
     * Background GL clear color. Default is light gray
     */
    val backgroundColor = floatArrayOf(0.2f, 0.2f, 0.2f, 1.0f)

    var gLView: StudyModelSurfaceView? = null

    var scene: StudySceneLoader? = null

    var handler: Handler? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(R.color.background_tab_pressed)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_align)
        dicTheme = intent.getIntExtra("theme", 0)
        initPresenter()
        studyAlignPresenter.initView()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun initPresenter(){
        studyAlignPresenter = StudyAlignPresenter()
        studyAlignPresenter.view = this
    }

    fun initView(datas : ArrayList<StudyAlignDatas>, scenes : ArrayList<StudySceneLoader>){
        //scene = StudySceneLoader(this)
        //scene!!.init()
        requestManager = Glide.with(this)
        studyAlignAdapter = StudyAlignAdapter(datas, this, requestManager)
        rv_study_align.layoutManager = GridLayoutManager(this, 3)
        rv_study_align.adapter = studyAlignAdapter
    }

    private fun setOnBinding(){

    }

}
