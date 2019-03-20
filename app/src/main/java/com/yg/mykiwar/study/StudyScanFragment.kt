package com.yg.mykiwar.study

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class StudyScanFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session?): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session!!.configure(config)
        this.arSceneView.setupSession(session)
        val studyScanActivity = activity as StudyScanActivity

        if(studyScanActivity.setUpAugmentedImageDb(config, session)){
            Log.v("들어옴", "success")
            Log.v("들어옴", "k")
        }else{
            Log.v("들어옴", "l")
            Log.v("들어옴", "fail")
        }
        return config
    }

}