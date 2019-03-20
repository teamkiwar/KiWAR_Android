package com.yg.mykiwar.study

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.card.StudyCardAdapter
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_card.*


class StudyCardActivity : AppCompatActivity() {

    var paramUri: Uri? = null
    var images : ArrayList<String> = ArrayList()
    var datas : ArrayList<Int> = ArrayList()

    var otherUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = this.resources.getColor(R.color.background_tab_pressed)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_card)

//        CurrentActivity.activity = this
//        paramUri = Uri.parse("assets://${packageName}/models/andy.obj")
//        otherUri = Uri.parse("assets://${packageName}/models/Mesh_Cat.obj")
        //initPresenter()

        datas.add(R.drawable.bighornsheep)
        datas.add(R.drawable.buffalo)
        datas.add(R.drawable.camel)
        datas.add(R.drawable.cat)
        datas.add(R.drawable.cow)
        datas.add(R.drawable.dog)
        datas.add(R.drawable.elephant)
        datas.add(R.drawable.feret)
        datas.add(R.drawable.fox)
        datas.add(R.drawable.gazelle)
        datas.add(R.drawable.goat)
        datas.add(R.drawable.horse)
        datas.add(R.drawable.lion)
        datas.add(R.drawable.mouse)
        datas.add(R.drawable.riverotter)
        datas.add(R.drawable.panda)
        datas.add(R.drawable.penguin)
        datas.add(R.drawable.pig)
        datas.add(R.drawable.raccoon)
        datas.add(R.drawable.sheep)
        datas.add(R.drawable.snake)

        for(animal in AnimalList.animalList)
            images.add(animal)

        vp_study_card.adapter = StudyCardAdapter(supportFragmentManager, paramUri, images.size, images, datas)
        //vp_study_card.offscreenPageLimit = 3
    }


}
