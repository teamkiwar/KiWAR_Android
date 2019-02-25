package com.yg.mykiwar.play.catchgame.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.yg.mykiwar.R

class PlayCatchViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    var catchAnswer : TextView = itemView.findViewById(R.id.tv_play_catch_answer)
}