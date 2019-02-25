package com.yg.mykiwar.study.card

import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class StudyCardAdapter(fm : FragmentManager, var model : Uri?, var itemSize : Int, var images : ArrayList<String>, var datas : ArrayList<Uri?>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return StudyCardFragment.create(images[position], datas[position])
    }

    override fun getCount(): Int = itemSize
}