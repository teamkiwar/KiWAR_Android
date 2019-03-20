package com.yg.mykiwar.util.presenter

import com.yg.mykiwar.study.StudyAlignActivity
import com.yg.mykiwar.study.align.StudyAlignDatas
import com.yg.mykiwar.study.model.StudySceneLoader

class StudyAlignPresenter : BasePresenter<StudyAlignActivity>() {

    lateinit var datas : ArrayList<StudyAlignDatas>
    lateinit var scenes : ArrayList<StudySceneLoader>
    fun initView(){
        datas = ArrayList()
        scenes = ArrayList()

//        datas.add(StudyAlignDatas(R.drawable.andy))
//        datas.add(StudyAlignDatas(R.drawable.cat))
//        datas.add(StudyAlignDatas(R.drawable.dog))
//        datas.add(StudyAlignDatas(R.drawable.giraffe))
//        datas.add(StudyAlignDatas(R.drawable.penguin))
//        datas.add(StudyAlignDatas(R.drawable.wolf))
//        datas.add(StudyAlignDatas(R.drawable.andy))
//        datas.add(StudyAlignDatas(R.drawable.cat))
//        datas.add(StudyAlignDatas(R.drawable.dog))
//        datas.add(StudyAlignDatas(R.drawable.giraffe))
//        datas.add(StudyAlignDatas(R.drawable.penguin))
//        datas.add(StudyAlignDatas(R.drawable.wolf))
//        datas.add(StudyAlignDatas(R.drawable.andy))
//        datas.add(StudyAlignDatas(R.drawable.cat))
//        datas.add(StudyAlignDatas(R.drawable.dog))
//        datas.add(StudyAlignDatas(R.drawable.giraffe))
//        datas.add(StudyAlignDatas(R.drawable.penguin))
//        datas.add(StudyAlignDatas(R.drawable.wolf))
//        datas.add(StudyAlignDatas(R.drawable.andy))
//        datas.add(StudyAlignDatas(R.drawable.cat))
//        datas.add(StudyAlignDatas(R.drawable.dog))
//        datas.add(StudyAlignDatas(R.drawable.giraffe))
//        datas.add(StudyAlignDatas(R.drawable.penguin))
//        datas.add(StudyAlignDatas(R.drawable.wolf))
//        datas.add(StudyAlignDatas(R.drawable.andy))
//        datas.add(StudyAlignDatas(R.drawable.cat))
//        datas.add(StudyAlignDatas(R.drawable.dog))
//        datas.add(StudyAlignDatas(R.drawable.giraffe))
//        datas.add(StudyAlignDatas(R.drawable.penguin))
//        datas.add(StudyAlignDatas(R.drawable.wolf))



        view!!.initView(datas, scenes)

    }
}