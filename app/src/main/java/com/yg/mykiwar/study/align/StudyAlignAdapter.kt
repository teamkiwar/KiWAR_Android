package com.yg.mykiwar.study.align

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.StudyCardActivity

class StudyAlignAdapter(var datas : ArrayList<StudyAlignDatas>, var context : Context, var requestManager : RequestManager) : RecyclerView.Adapter<StudyAlignViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyAlignViewHolder {
        val mainView : View = LayoutInflater.from(parent.context).inflate(R.layout.study_align_item,parent,false)
        return StudyAlignViewHolder(mainView)
    }

    override fun getItemCount(): Int = datas.size
    override fun onBindViewHolder(holder: StudyAlignViewHolder, position: Int) {
        holder.alignItem.setOnClickListener {
            context.startActivity(Intent(context, StudyCardActivity::class.java))
        }
        requestManager.load(datas[position].path).into(holder.alignPreView)
    }
}