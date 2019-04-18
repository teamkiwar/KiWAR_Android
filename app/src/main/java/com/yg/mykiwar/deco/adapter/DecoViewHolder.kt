package com.yg.mykiwar.deco.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.yg.mykiwar.R

class DecoViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
    var model : ImageView = itemView.findViewById(R.id.image_deco_model)
}