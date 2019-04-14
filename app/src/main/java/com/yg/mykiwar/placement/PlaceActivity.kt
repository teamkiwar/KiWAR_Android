package com.yg.mykiwar.placement

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.yg.mykiwar.R
import com.yg.mykiwar.placement.draw.MainRenderer
import kotlinx.android.synthetic.main.activity_place.*


class PlaceActivity : AppCompatActivity() {
    private val TAG = PlaceActivity::class.java.simpleName

    private var mCheckBox: CheckBox? = null
    private var mSurfaceView: GLSurfaceView? = null
    private var mRenderer: MainRenderer? = null

    private var mUserRequestedInstall = true

    private var mSession: Session? = null
    private var mConfig: Config? = null

    private val mProjMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    private var mLastX: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()
    private val mLastPoint = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var mNewPath = false
    private var mPointAdded = false

    private val MIN_DISTANCE = 0.000625f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        setContentView(R.layout.activity_place)
        //mCheckBox = findViewById(R.id.check_box) as CheckBox
        //mSurfaceView = findViewById(R.id.gl_surface_view) as GLSurfaceView

        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {}

            override fun onDisplayChanged(displayId: Int) {
                synchronized(this) {
                    mRenderer!!.onDisplayChanged()
                }
            }

            override fun onDisplayRemoved(displayId: Int) {}
        }, null)

        mRenderer = MainRenderer(MainRenderer.RenderCallback {
            if (mRenderer!!.isViewportChanged) {
                val display = windowManager.defaultDisplay
                val displayRotation = display.rotation
                mRenderer!!.updateSession(mSession, displayRotation)
            }

            mSession!!.setCameraTextureName(mRenderer!!.textureId)

            var frame: Frame? = null
            try {
                frame = mSession!!.update()
            } catch (e: CameraNotAvailableException) {
                e.printStackTrace()
            }

            if (frame!!.hasDisplayGeometryChanged()) {
                mRenderer!!.transformDisplayGeometry(frame)
            }

            val pointCloud = frame.acquirePointCloud()
            mRenderer!!.updatePointCloud(pointCloud)
            pointCloud.release()

            val camera = frame.camera
            camera.getProjectionMatrix(mProjMatrix, 0, 0.1f, 100.0f)
            camera.getViewMatrix(mViewMatrix, 0)

            mRenderer!!.setProjectionMatrix(mProjMatrix)
            mRenderer!!.updateViewMatrix(mViewMatrix)

            if (check_box.isChecked) {
                val screenPoint = getScreenPoint(mLastX, mLastY,
                        mRenderer!!.width.toFloat(), mRenderer!!.height.toFloat(),
                        mProjMatrix, mViewMatrix)
                if (mNewPath) {
                    mNewPath = false
                    mRenderer!!.addPath(screenPoint[0], screenPoint[1], screenPoint[2])
                    mLastPoint[0] = screenPoint[0]
                    mLastPoint[1] = screenPoint[1]
                    mLastPoint[2] = screenPoint[2]
                } else if (mPointAdded) {
                    if (checkDistance(screenPoint[0], screenPoint[1], screenPoint[2],
                                    mLastPoint[0], mLastPoint[1], mLastPoint[2])) {
                        mRenderer!!.addPoint(screenPoint[0], screenPoint[1], screenPoint[2])
                        mLastPoint[0] = screenPoint[0]
                        mLastPoint[1] = screenPoint[1]
                        mLastPoint[2] = screenPoint[2]
                    }
                }
            } else {
                if (mNewPath) {
                    val results = frame.hitTest(mLastX, mLastY)
                    for (result in results) {
                        val pose = result.hitPose
                        mNewPath = false
                        mRenderer!!.addPath(pose.tx(), pose.ty(), pose.tz())
                        mLastPoint[0] = pose.tx()
                        mLastPoint[1] = pose.ty()
                        mLastPoint[2] = pose.tz()
                        break
                    }
                } else if (mPointAdded) {
                    val results = frame.hitTest(mLastX, mLastY)
                    for (result in results) {
                        val pose = result.hitPose
                        if (checkDistance(pose.tx(), pose.ty(), pose.tz(),
                                        mLastPoint[0], mLastPoint[1], mLastPoint[2])) {
                            mRenderer!!.addPoint(pose.tx(), pose.ty(), pose.tz())
                            mLastPoint[0] = pose.tx()
                            mLastPoint[1] = pose.ty()
                            mLastPoint[2] = pose.tz()
                            break
                        }
                    }
                    mPointAdded = false
                }
            }
        })
        gl_surface_view.preserveEGLContextOnPause = true
        gl_surface_view.setEGLContextClientVersion(2)
        gl_surface_view.setRenderer(mRenderer)
        gl_surface_view.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        transfer()
    }

    fun transfer(){
        btn_place_transfer.setOnClickListener {
            //val picture = mSession!!.update().acquireCameraImage()
            gl_surface_view.setZOrderOnTop(true)
        }
    }

    override fun onPause() {
        super.onPause()
        gl_surface_view!!.onPause()
        mSession!!.pause()
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()

        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        mSession = Session(this)
                        Log.v(TAG, "ARCore Session created.")
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                        Log.v(TAG, "ARCore should be installed.")
                    }
                    null->{
                        Log.v(TAG, "null.")
                    }
                }
            }
        } catch (e: UnsupportedOperationException) {
            Log.e(TAG, e.message)
        } catch (e: UnavailableApkTooOldException) {
            e.printStackTrace()
        } catch (e: UnavailableDeviceNotCompatibleException) {
            e.printStackTrace()
        } catch (e: UnavailableUserDeclinedInstallationException) {
            e.printStackTrace()
        } catch (e: UnavailableArcoreNotInstalledException) {
            e.printStackTrace()
        } catch (e: UnavailableSdkTooOldException) {
            e.printStackTrace()
        }


        mConfig = Config(mSession)
        if (!mSession!!.isSupported(mConfig)) {
            Log.v(TAG, "This device is not support ARCore.")
        }
        mSession!!.configure(mConfig)
        try {
            mSession!!.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }

        gl_surface_view!!.onResume()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mLastX = event!!.x
        mLastY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mNewPath = true
                mPointAdded = true
            }
            MotionEvent.ACTION_MOVE -> mPointAdded = true
            MotionEvent.ACTION_UP -> mPointAdded = false
        }
        return true
    }


    fun onUndoButtonClick(view: View) {
        mRenderer!!.removePath()
    }

    fun getScreenPoint(x: Float, y: Float, w: Float, h: Float,
                       projMat: FloatArray, viewMat: FloatArray): FloatArray {
        var x = x
        var y = y
        val position = FloatArray(3)
        val direction = FloatArray(3)

        x = x * 2 / w - 1.0f
        y = (h - y) * 2 / h - 1.0f

        val viewProjMat = FloatArray(16)
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0)

        val invertedMat = FloatArray(16)
        Matrix.setIdentityM(invertedMat, 0)
        Matrix.invertM(invertedMat, 0, viewProjMat, 0)

        val farScreenPoint = floatArrayOf(x, y, 1.0f, 1.0f)
        val nearScreenPoint = floatArrayOf(x, y, -1.0f, 1.0f)
        val nearPlanePoint = FloatArray(4)
        val farPlanePoint = FloatArray(4)

        Matrix.multiplyMV(nearPlanePoint, 0, invertedMat, 0, nearScreenPoint, 0)
        Matrix.multiplyMV(farPlanePoint, 0, invertedMat, 0, farScreenPoint, 0)

        position[0] = nearPlanePoint[0] / nearPlanePoint[3]
        position[1] = nearPlanePoint[1] / nearPlanePoint[3]
        position[2] = nearPlanePoint[2] / nearPlanePoint[3]

        direction[0] = farPlanePoint[0] / farPlanePoint[3] - position[0]
        direction[1] = farPlanePoint[1] / farPlanePoint[3] - position[1]
        direction[2] = farPlanePoint[2] / farPlanePoint[3] - position[2]

        normalize(direction)

        position[0] += direction[0] * 0.1f
        position[1] += direction[1] * 0.1f
        position[2] += direction[2] * 0.1f

        return position
    }

    private fun normalize(v: FloatArray) {
        val norm = Math.sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble())
        v[0] /= norm.toFloat()
        v[1] /= norm.toFloat()
        v[2] /= norm.toFloat()
    }

    fun checkDistance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Boolean {
        val x = x1 - x2
        val y = y1 - y2
        val z = z1 - z2
        return Math.sqrt((x * x + y * y + z * z).toDouble()) > MIN_DISTANCE
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}
