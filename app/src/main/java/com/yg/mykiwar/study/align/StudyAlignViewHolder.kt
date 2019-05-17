package com.yg.mykiwar.study.align

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.yg.mykiwar.R

class StudyAlignViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    var alignItem : LinearLayout = itemView.findViewById(R.id.study_align_item)
    var alignPreView : ImageView = itemView.findViewById(R.id.imge_study_align)
    var alignName : TextView = itemView.findViewById(R.id.name_study_align)

}