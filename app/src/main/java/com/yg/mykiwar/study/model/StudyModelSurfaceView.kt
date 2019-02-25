package com.yg.mykiwar.study.model

import android.opengl.GLSurfaceView
import com.yg.mykiwar.study.StudyAlignActivity

class StudyModelSurfaceView(var parent : StudyAlignActivity) : GLSurfaceView(parent) {
    var mRenderer: StudyModelRender

    init {
        setEGLContextClientVersion(2)

        // This is the actual renderer of the 3D space
        mRenderer = StudyModelRender(this)
        setRenderer(mRenderer)

    }
}