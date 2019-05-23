package com.yg.mykiwar.study

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.align.StudyAlignAdapter
import com.yg.mykiwar.study.align.StudyAlignDatas
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_card.*


class StudyCardActivity : AppCompatActivity() {

    var paramUri: Uri? = null
    var images : ArrayList<String> = ArrayList()
    var datas : ArrayList<StudyAlignDatas> = ArrayList()
    lateinit var studyAlignAdapter: StudyAlignAdapter
    lateinit var requestManager : RequestManager

    var otherUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val window = this.window
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_card)

        btn_study_card_back.setOnClickListener {
            finish()
        }

        btn_study_card_toscan.setOnClickListener {
            startActivity(Intent(this, StudyScanActivity::class.java))
        }

        datas.add(StudyAlignDatas(R.drawable.bighornsheep, "큰뿔양"))
        datas.add(StudyAlignDatas(R.drawable.buffalo, "버팔로"))
        datas.add(StudyAlignDatas(R.drawable.camel, "낙타"))
        datas.add(StudyAlignDatas(R.drawable.cat, "고양이"))
        datas.add(StudyAlignDatas(R.drawable.cow, "소"))
        datas.add(StudyAlignDatas(R.drawable.dog, "개"))
        datas.add(StudyAlignDatas(R.drawable.elephant, "코끼리"))
        datas.add(StudyAlignDatas(R.drawable.feret, "퍼렛"))
        datas.add(StudyAlignDatas(R.drawable.fox, "여우"))
        datas.add(StudyAlignDatas(R.drawable.gazelle, "가젤"))
        datas.add(StudyAlignDatas(R.drawable.goat, "염소"))
        datas.add(StudyAlignDatas(R.drawable.horse, "말"))
        datas.add(StudyAlignDatas(R.drawable.lion, "사자"))
        datas.add(StudyAlignDatas(R.drawable.mouse, "쥐"))
        datas.add(StudyAlignDatas(R.drawable.riverotter, "수달"))
        datas.add(StudyAlignDatas(R.drawable.panda, "팬더"))
        datas.add(StudyAlignDatas(R.drawable.penguin, "펭귄"))
        datas.add(StudyAlignDatas(R.drawable.pig, "돼지"))
        datas.add(StudyAlignDatas(R.drawable.raccoon, "라쿤"))
        datas.add(StudyAlignDatas(R.drawable.sheep, "양"))
        datas.add(StudyAlignDatas(R.drawable.snake, "뱀"))

        for(animal in AnimalList.animalList)
            images.add(animal)

        requestManager = Glide.with(this)
        studyAlignAdapter = StudyAlignAdapter(datas, this, requestManager)
        list_card_animal.layoutManager = GridLayoutManager(this, 2)
        list_card_animal.adapter = studyAlignAdapter
    }


}
