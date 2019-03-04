package com.yg.mykiwar.study

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.card.CurrentActivity
import com.yg.mykiwar.study.card.StudyCardAdapter
import com.yg.mykiwar.study.card.StudyCardSceneLoader
import com.yg.mykiwar.study.model.StudyModelSurfaceView
import kotlinx.android.synthetic.main.activity_study_card.*


class StudyCardActivity : AppCompatActivity() {

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

    var scene: StudyCardSceneLoader? = null

    var handler: Handler? = null

    var images : ArrayList<String> = ArrayList()
    var datas : ArrayList<Uri?> = ArrayList()

    var otherUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(R.color.background_tab_pressed)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_card)
        CurrentActivity.activity = this
        paramUri = Uri.parse("assets://${packageName}/models/andy.obj")
        otherUri = Uri.parse("assets://${packageName}/models/Mesh_Cat.obj")
        //initPresenter()

        datas.add(Uri.parse("assets://${packageName}/models/andy.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/Mesh_Cat.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/dog.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/Giraffe.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/penguin.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/Wolf.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/andy.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/cat.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/dog.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/Giraffe.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/penguin.obj"))
        datas.add(Uri.parse("assets://${packageName}/models/Wolf.obj"))



//        scene = StudyCardSceneLoader(this)
//        scene!!.init()

        //gLView = StudyModelSurfaceView(this)

        //setContentView(gLView)

        // "assets://org.andresoviedo.dddmodel2/models/ToyPlane.obj
        //studyAlignPresenter.initView(paramUri!!, this)

        images.add("앤디")
        images.add("고양이")
        images.add("개")
        images.add("기린")
        images.add("펭귄")
        images.add("늑대")
        images.add("앤디")
        images.add("고양이")
        images.add("개")
        images.add("기린")
        images.add("펭귄")
        images.add("늑대")


        vp_study_card.adapter = StudyCardAdapter(supportFragmentManager, paramUri, images.size, images, datas)
        //vp_study_card.offscreenPageLimit = 3
    }


}
