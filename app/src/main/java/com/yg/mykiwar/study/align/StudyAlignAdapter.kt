package com.yg.mykiwar.study.align

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.StudyScanActivity



class StudyAlignAdapter(var datas : ArrayList<StudyAlignDatas>, var context : Context, var requestManager : RequestManager) : RecyclerView.Adapter<StudyAlignViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyAlignViewHolder {
        val mainView : View = LayoutInflater.from(parent.context).inflate(R.layout.study_align_item,parent,false)
        return StudyAlignViewHolder(mainView)
    }

    override fun getItemCount(): Int = datas.size
    override fun onBindViewHolder(holder: StudyAlignViewHolder, position: Int) {
        holder.alignItem.setOnClickListener {
            val intent = Intent(context, StudyScanActivity::class.java)
            intent.putExtra("name", datas[position].name)
            context.startActivity(intent)
        }
//        holder.alignPreView.setBackgroundResource(datas[position].path)
        val drawable = context.getDrawable(R.drawable.card_round2) as GradientDrawable
        requestManager.load(datas[position].path).into(holder.alignPreView)
        holder.alignPreView.background = drawable
        holder.alignPreView.clipToOutline = true
        holder.alignName.text = datas[position].name
    }
}
