package com.yg.mykiwar.study.card

import android.opengl.GLSurfaceView

class StudyCardSurfaceView(var parent : StudyCardFragment) : GLSurfaceView(parent.context) {
    var mRenderer: StudyCardRenderer

    init {
        setEGLContextClientVersion(2)

        // This is the actual renderer of the 3D space
        mRenderer = StudyCardRenderer(this)
        setRenderer(mRenderer)

    }
}