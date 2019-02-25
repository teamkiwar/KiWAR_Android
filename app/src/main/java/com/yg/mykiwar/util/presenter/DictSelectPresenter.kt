package com.yg.mykiwar.util.presenter

import com.yg.mykiwar.study.StudySelectActivity


class DictSelectPresenter : BasePresenter<StudySelectActivity>(){
    fun selectTheme(theme : Int){
        when(theme){
            0->{
                view!!.selectTheme(0)
            }
            1->{
                view!!.selectTheme(1)
            }
            2->{
                view!!.selectTheme(2)
            }
            3->{
                view!!.selectTheme(3)
            }
        }
    }
}