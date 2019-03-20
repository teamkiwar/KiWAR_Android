package com.yg.mykiwar.study.card

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yg.mykiwar.R
import com.yg.mykiwar.study.StudyScanActivity
import kotlinx.android.synthetic.main.fragment_study_card.*

class StudyCardFragment : Fragment() {

    lateinit var scene : StudyCardSceneLoader
    var gLView : StudyCardSurfaceView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_study_card, container, false)
    }

    override fun onStart() {
        super.onStart()
        val name = arguments!!.getString("imageName")
        tv_study_card.text = name
        img_study_card.setBackgroundResource(arguments!!.getInt("imageUri"))
        btn_study_scan.setOnClickListener {
            val intent = Intent(context, StudyScanActivity::class.java)
            intent.putExtra("name", name)
            activity!!.startActivity(intent)
        }
    }

    companion object {
        fun create(imageName: String, imageUri : Int): StudyCardFragment {
            val fragment = StudyCardFragment()
            val args = Bundle()
            args.putString("imageName", imageName)
            args.putInt("imageUri", imageUri)
            fragment.arguments = args
            return fragment
        }
    }
}