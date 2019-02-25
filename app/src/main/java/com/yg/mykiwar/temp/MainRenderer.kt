package com.yg.mykiwar.temp

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.PointCloud
import com.google.ar.core.Session

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainRenderer(var context : Context, var callback : RenderCallback) : GLSurfaceView.Renderer {

    private val TAG = MainRenderer::class.java.simpleName

    private var mViewportChanged: Boolean = false
    private var mViewportWidth: Int = 0
    private var mViewportHeight: Int = 0

    private val mCamera: CameraRenderer
    private val mPointCloud: PointCloudRendererT
    private val mPlane: PlaneRendererT

    private val mObj: ObjectRendererT

    private val mRenderCallback: RenderCallback

    interface RenderCallback {
        fun preRender()
    }

    init {
        mCamera = CameraRenderer()

        mPointCloud = PointCloudRendererT()

        mPlane = PlaneRendererT(Color.GRAY, 0.5f)

        mObj = ObjectRendererT(context, "models/andy.obj", "models/andy.png")

        mRenderCallback = callback
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        mRenderCallback.preRender()

        GLES20.glDepthMask(false)
        mCamera.draw()
        GLES20.glDepthMask(true)

        mPointCloud.draw()

        mPlane.draw()

        mObj.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mViewportChanged = true
        mViewportWidth = width
        mViewportHeight = height
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)

        mCamera.init()

        mPointCloud.init()

        mPlane.init()

        mObj.init()
    }

    fun getTextureId(): Int {
        return mCamera?.getTextureId() ?: -1
    }

    fun onDisplayChanged() {
        mViewportChanged = true
    }

    fun isViewportChanged(): Boolean {
        return mViewportChanged
    }

    fun getWidth(): Int {
        return mViewportWidth
    }

    fun getHeight(): Int {
        return mViewportHeight
    }

    fun updateSession(session: Session, displayRotation: Int) {
        if (mViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight)
            mViewportChanged = false
        }
    }

    fun transformDisplayGeometry(frame: Frame) {
        mCamera.transformDisplayGeometry(frame)
    }

    fun updatePointCloud(pointCloud: PointCloud) {
        mPointCloud.update(pointCloud)
    }

    fun updatePlane(plane: Plane) {
        mPlane.update(plane)
    }


    fun setObjModelMatrix(matrix: FloatArray) {
        mObj.setModelMatrix(matrix)
    }

    fun setProjectionMatrix(matrix: FloatArray) {
        mPointCloud.setProjectionMatrix(matrix)
        mPlane.setProjectionMatrix(matrix)
        mObj.setProjectionMatrix(matrix)
    }

    fun updateViewMatrix(matrix: FloatArray) {
        mPointCloud.setViewMatrix(matrix)
        mPlane.setViewMatrix(matrix)
        mObj.setViewMatrix(matrix)
    }

    fun getObjMinPoint(): FloatArray {
        return mObj.getMinPoint()
    }

    fun getObjMaxPoint(): FloatArray {
        return mObj.getMaxPoint()
    }


}