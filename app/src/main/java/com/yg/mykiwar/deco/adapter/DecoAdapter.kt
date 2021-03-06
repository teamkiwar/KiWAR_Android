package com.yg.mykiwar.deco.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.yg.mykiwar.R

class DecoAdapter(var modelList : ArrayList<Int>, var requestManager: RequestManager) : RecyclerView.Adapter<DecoViewHolder>() {
    private var onItemClick : View.OnClickListener? = null
    var height = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DecoViewHolder {
        val mainView : View = LayoutInflater.from(parent.context).inflate(R.layout.deco_model_item,parent,false)
        mainView.setOnClickListener(onItemClick)
        height = parent.measuredWidth / 4

        return DecoViewHolder(mainView)
    }

    override fun getItemCount(): Int = modelList.size

    override fun onBindViewHolder(holder: DecoViewHolder, position: Int) {
        requestManager.load(modelList[position]).into(holder.model)

        val params = holder.model.layoutParams
        params.height = height
        holder.model.layoutParams = params
    }


    fun setOnItemClickListener(l:View.OnClickListener){
        onItemClick = l
    }
}