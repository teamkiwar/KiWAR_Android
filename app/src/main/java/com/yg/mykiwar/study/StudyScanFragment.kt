package com.yg.mykiwar.study

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
            //
        }else{
            //
        }
        return config
    }

}