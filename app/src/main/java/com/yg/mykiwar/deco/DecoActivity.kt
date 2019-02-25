package com.yg.mykiwar.deco

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.yg.mykiwar.R
import com.yg.mykiwar.util.helper.DisplayRotationHelper
import com.yg.mykiwar.util.helper.FullScreenHelper
import com.yg.mykiwar.util.helper.SnackbarHelper
import com.yg.mykiwar.util.helper.TapHelper
import com.yg.mykiwar.util.renderer.BackgroundRenderer
import com.yg.mykiwar.util.renderer.ObjectRenderer
import com.yg.mykiwar.util.renderer.PlaneRenderer
import com.yg.mykiwar.util.renderer.PointCloudRenderer
import java.io.IOException
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DecoActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private val TAG = DecoActivity::class.java.getSimpleName()

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private var surfaceView: GLSurfaceView? = null

    private var installRequested: Boolean = false

    private var session: Session? = null
    private val messageSnackbarHelper = SnackbarHelper()
    private var displayRotationHelper: DisplayRotationHelper? = null
    private var tapHelper: TapHelper? = null

    private val backgroundRenderer = BackgroundRenderer()
    private val virtualObject = ObjectRenderer()
    private val virtualObjectShadow = ObjectRenderer()
    private val planeRenderer = PlaneRenderer()
    private val pointCloudRenderer = PointCloudRenderer()

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private val anchorMatrix = FloatArray(16)
    private val DEFAULT_COLOR = floatArrayOf(0f, 0f, 0f, 0f)

    private class ColoredAnchor(val anchor: Anchor, val color: FloatArray)

    private val anchors = ArrayList<ColoredAnchor>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deco)

        surfaceView = findViewById(R.id.surfaceview)
        displayRotationHelper = DisplayRotationHelper(/*context=*/this)

        // Set up tap listener.
        tapHelper = TapHelper(/*context=*/this)
        surfaceView!!.setOnTouchListener(tapHelper)

        // Set up renderer.
        surfaceView!!.setPreserveEGLContextOnPause(true)
        surfaceView!!.setEGLContextClientVersion(2)
        surfaceView!!.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.
        surfaceView!!.setRenderer(this)
        surfaceView!!.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)

        installRequested = false
    }

    override fun onResume() {
        super.onResume()

        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)!!) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                    }
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
//                if (!CameraPermissionHelper.hasCameraPermission(this)) {
//                    CameraPermissionHelper.requestCameraPermission(this)
//                    return
//                }

                // Create the session.
                session = Session(/* context= */this)
                Log.v("야야", "세션 잘 생김")

            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
                Log.v("야야", message)

            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
                Log.v("야야", message)

            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
                Log.v("야야", message)

            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
                Log.v("야야", message)

            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
                Log.v("야야", message)

            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
                Log.v("야야", message)

            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.")
            session = null
            return
        }

        surfaceView!!.onResume()
        displayRotationHelper!!.onResume()

        messageSnackbarHelper.showMessage(this, "Searching for surfaces...")
    }


    public override fun onPause() {
        super.onPause()
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper!!.onPause()
            surfaceView!!.onPause()
            session!!.pause()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }


    override fun onDrawFrame(gl: GL10?) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (session == null) {
            return
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper!!.updateSessionIfNeeded(session!!)

        try {
            session!!.setCameraTextureName(backgroundRenderer.textureId)

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            val frame = session!!.update()
            val camera = frame.camera

            // Handle one tap per frame.
            handleTap(frame, camera)

            // Draw background.
            backgroundRenderer.draw(frame)

            // If not tracking, don't draw 3d objects.
            if (camera.trackingState == TrackingState.PAUSED) {
                return
            }

            // Get projection matrix.
            val projmtx = FloatArray(16)
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)

            // Get camera matrix and draw.
            val viewmtx = FloatArray(16)
            camera.getViewMatrix(viewmtx, 0)

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            val colorCorrectionRgba = FloatArray(4)
            frame.lightEstimate.getColorCorrection(colorCorrectionRgba, 0)

            // Visualize tracked points.
            val pointCloud = frame.acquirePointCloud()
            pointCloudRenderer.update(pointCloud)
            pointCloudRenderer.draw(viewmtx, projmtx)

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release()

            // Check if we detected at least one plane. If so, hide the loading message.
            if (messageSnackbarHelper.isShowing) {
                for (plane in session!!.getAllTrackables(Plane::class.java)) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        messageSnackbarHelper.hide(this)
                        break
                    }
                }
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session!!.getAllTrackables(Plane::class.java), camera.displayOrientedPose, projmtx)

            // Visualize anchors created by touch.
            val scaleFactor = 1.0f
            Log.v("갸악", "온 드로우 옴")
            for (coloredAnchor in anchors) {
                if (coloredAnchor.anchor.trackingState != TrackingState.TRACKING) {
                    continue
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                coloredAnchor.anchor.pose.toMatrix(anchorMatrix, 0)

//                Log.v("anchorMatrix 값 : ", anchorMatrix[0].toString() + " " + anchorMatrix[1].toString()
//                        + " " + anchorMatrix[2].toString()+ " " + anchorMatrix[3].toString()
//                        + " " + anchorMatrix[4].toString()+ " " + anchorMatrix[5].toString()
//                        + " " + anchorMatrix[6].toString()+ " " + anchorMatrix[7].toString()
//                        + " " + anchorMatrix[8].toString()+ " " + anchorMatrix[9].toString()
//                        + " " + anchorMatrix[10].toString()+ " " + anchorMatrix[11].toString()
//                        + " " + anchorMatrix[12].toString()+ " " + anchorMatrix[13].toString()
//                        + " " + anchorMatrix[14].toString()+ " " + anchorMatrix[15].toString())
//
//                Log.v("viewmtx 값 : ", viewmtx[0].toString() + " " + viewmtx[1].toString() +
//                " " + viewmtx[2].toString() + " " + viewmtx[3].toString() + " " + viewmtx[4].toString() +
//                        " " + viewmtx[5].toString() + " " + viewmtx[6].toString() + " " + viewmtx[7].toString() +
//                        " " + viewmtx[8].toString() + " " + viewmtx[9].toString() + " " + viewmtx[10].toString() +
//                        " " + viewmtx[11].toString() + " " + viewmtx[12].toString()+ " " + viewmtx[13].toString() +
//                        " " + viewmtx[14].toString() + " " + viewmtx[15].toString())
//
//                Log.v("projmtx 값 : ", projmtx[0].toString() + " " + projmtx[1].toString() +
//                        " " + projmtx[2].toString() + " " + projmtx[3].toString() + " " + projmtx[4].toString() +
//                        " " + projmtx[5].toString() + " " + projmtx[6].toString() + " " + projmtx[7].toString() +
//                        " " + projmtx[8].toString() + " " + projmtx[9].toString() + " " + projmtx[10].toString() +
//                        " " + projmtx[11].toString() + " " + projmtx[12].toString() + " " + projmtx[13].toString() +
//                        " " + projmtx[14].toString() + " " + projmtx[15].toString())


                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor)
                virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor)
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, coloredAnchor.color)
                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, coloredAnchor.color)
            }

        } catch (t: Throwable) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        displayRotationHelper!!.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/this)
            planeRenderer.createOnGlThread(/*context=*/this, "models/trigrid.png")
            pointCloudRenderer.createOnGlThread(/*context=*/this)

            virtualObject.createOnGlThread(/*context=*/this, "models/andy.obj", "models/andy.png")
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f)

            virtualObjectShadow.createOnGlThread(
                    /*context=*/ this, "models/andy_shadow.obj", "models/andy_shadow.png")
            virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow)
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f)

        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    private fun handleTap(frame: Frame, camera: Camera) {
        val tap : MotionEvent? = tapHelper!!.poll()
        if (tap != null && camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                val trackable = hit.trackable
                // Creates an anchor if a plane or an oriented point was hit.
                if ((trackable is Plane
                                && trackable.isPoseInPolygon(hit.hitPose)
                                && PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0) || trackable is Point && trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {
                    // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                    // Cap the number of objects created. This avoids overloading both the
                    // rendering system and ARCore.
                    if (anchors.size >= 20) {
                        anchors[0].anchor.detach()
                        anchors.removeAt(0)
                    }

                    // Assign a color to the object for rendering based on the trackable type
                    // this anchor attached to. For AR_TRACKABLE_POINT, it's blue color, and
                    // for AR_TRACKABLE_PLANE, it's green color.
                    val objColor: FloatArray
                    if (trackable is Point) {
                        objColor = floatArrayOf(66.0f, 133.0f, 244.0f, 255.0f)
                    } else if (trackable is Plane) {
                        objColor = floatArrayOf(139.0f, 195.0f, 74.0f, 255.0f)
                    } else {
                        objColor = DEFAULT_COLOR
                    }

                    // Adding an Anchor tells ARCore that it should track this position in
                    // space. This anchor is created on the Plane to place the 3D model
                    // in the correct position relative both to the world and to the plane.
                    anchors.add(ColoredAnchor(hit.createAnchor(), objColor))
                    break
                }
            }
        }
    }

}
