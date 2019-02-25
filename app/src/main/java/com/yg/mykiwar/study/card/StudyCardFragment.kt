package com.yg.mykiwar.study.card

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.fragment_study_card.*

class StudyCardFragment : Fragment() {

    lateinit var scene : StudyCardSceneLoader
    var gLView : StudyCardSurfaceView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_study_card, container, false)
        return v
    }

    override fun onStart() {
        super.onStart()
        tv_study_card.text = arguments!!.getString("imageName")
        Log.v("왔다", arguments!!.getString("imageUri"))
        scene = StudyCardSceneLoader(this, Uri.parse(arguments!!.getString("imageUri")))
        scene.init()
        gLView = StudyCardSurfaceView(this)
//        scene = StudyCardSceneLoader(activity as StudyCardActivity, Uri.parse(arguments!!.getString("imageUri")))
//        CurrentActivity.scene = scene
//        scene.init()
        layout_study_card.addView(gLView)
    }

    companion object {
        fun create(imageName: String, imageUri : Uri?): StudyCardFragment {
            val fragment = StudyCardFragment()
            val args = Bundle()
            args.putString("imageName", imageName)
            args.putString("imageUri", imageUri!!.toString())
            fragment.arguments = args
            return fragment
        }
    }



}