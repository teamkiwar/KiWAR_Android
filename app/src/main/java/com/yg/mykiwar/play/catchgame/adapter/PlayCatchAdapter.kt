package com.yg.mykiwar.play.catchgame.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yg.mykiwar.R

class PlayCatchAdapter(var answerItems : ArrayList<String>) : RecyclerView.Adapter<PlayCatchViewHolder>() {

    private var onItemClick : View.OnClickListener? = null

    fun setOnItemClickListener(l:View.OnClickListener){
        onItemClick = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayCatchViewHolder {
        val mainView : View = LayoutInflater.from(parent.context).inflate(R.layout.play_catch_item,parent,false)
        mainView.setOnClickListener(onItemClick)
        return PlayCatchViewHolder(mainView)
    }

    override fun getItemCount(): Int = answerItems.size

    override fun onBindViewHolder(holder: PlayCatchViewHolder, position: Int) {
        holder.catchAnswer.text = answerItems[position]
    }
}