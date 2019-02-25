package com.yg.mykiwar.study.model

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.yg.engine.android_3d_model_engine.animation.Animator
import com.yg.engine.android_3d_model_engine.collision.CollisionDetection
import com.yg.engine.android_3d_model_engine.model.Camera
import com.yg.engine.android_3d_model_engine.model.Object3DData
import com.yg.engine.android_3d_model_engine.services.LoaderTask
import com.yg.engine.android_3d_model_engine.services.Object3DBuilder
import com.yg.engine.android_3d_model_engine.services.collada.ColladaLoaderTask
import com.yg.engine.android_3d_model_engine.services.stl.STLLoaderTask
import com.yg.engine.android_3d_model_engine.services.wavefront.WavefrontLoaderTask
import com.yg.engine.util.android.ContentUtils
import com.yg.engine.util.io.IOUtils
import com.yg.mykiwar.study.StudyAlignActivity
import java.io.IOException
import java.lang.Exception
import java.util.*

class StudySceneLoader(var parent : StudyAlignActivity) : LoaderTask.Callback {

    /**
     * Default model color: yellow
     */
    private val DEFAULT_COLOR = floatArrayOf(1.0f, 1.0f, 0f, 1.0f)

    var objects: List<Object3DData> = ArrayList()

    var camera: Camera? = null
    /**
     * Whether to draw objects as wireframes
     */
    var drawWireframe = false
    /**
     * Whether to draw using points
     */
    var drawingPoints = false
    /**
     * Whether to draw bounding boxes around objects
     */
    var drawBoundingBox = false
    /**
     * Whether to draw face normals. Normally used to debug models
     */
    val drawNormals = false
    /**
     * Whether to draw using textures
     */
    var drawTextures = true
    /**
     * Light toggle feature: we have 3 states: no light, light, light + rotation
     */
    var rotatingLight = true
    /**
     * Light toggle feature: whether to draw using lights
     */
    var drawLighting = true
    /**
     * Animate model (dae only) or not
     */
    var animateModel = true
    /**
     * Draw skeleton or not
     */
    var drawSkeleton = false
    /**
     * Toggle collision detection
     */
    var isCollision = false
    /**
     * Toggle 3d anaglyph
     */
    val isAnaglyph = false
    /**
     * Object selected by the user
     */
    var selectedObject: Object3DData? = null
    /**
     * Initial light position
     */
    val lightPosition = floatArrayOf(0f, 0f, 6f, 1f)
    /**
     * Light bulb 3d data
     */
    val lightPoint = Object3DBuilder.buildPoint(lightPosition).setId("light")
    /**
     * Animator
     */
    private val animator = Animator()
    /**
     * Did the user touched the model for the first time?
     */
    private var userHasInteracted: Boolean = false
    /**
     * time when model loading has started (for stats)
     */
    private var startTime: Long = 0

    override fun onStart() {
        ContentUtils.setThreadActivity(parent)
    }

    override fun onLoadError(ex: Exception?) {
        Log.e("SceneLoader", ex!!.message, ex)
        ContentUtils.setThreadActivity(null)
    }

    override fun onLoadComplete(datas: MutableList<Object3DData>?) {
        // TODO: move texture load to LoaderTask
        for (data in datas!!) {
            if (data.getTextureData() == null && data.getTextureFile() != null) {
                Log.i("LoaderTask", "Loading texture... " + data.getTextureFile())
                try {
                    ContentUtils.getInputStream(data.getTextureFile()).use({ stream ->
                        if (stream != null) {
                            data.setTextureData(IOUtils.read(stream))
                        }
                    })
                } catch (ex: IOException) {
                    data.addError("Problem loading texture " + data.getTextureFile())
                }

            }
        }
        // TODO: move error alert to LoaderTask
        val allErrors = ArrayList<String>()
        for (data in datas!!) {
            addObject(data)
            allErrors.addAll(data.getErrors())
        }
        if (!allErrors.isEmpty()) {
        }
        val elapsed = ((SystemClock.uptimeMillis() - startTime) / 1000).toString() + " secs"
        ContentUtils.setThreadActivity(null)
    }



    fun init(){
        // Camera to show a point of view
        camera = Camera()

        if (parent.paramUri == null) {
            return
        }

        startTime = SystemClock.uptimeMillis()
        val uri = parent.paramUri
        Log.i("Object3DBuilder", "Loading model $uri. async and parallel..")
        if (uri.toString().toLowerCase().endsWith(".obj") || parent.paramType == 0) {
            WavefrontLoaderTask(parent, uri, this).execute()
        } else if (uri.toString().toLowerCase().endsWith(".stl") || parent.paramType == 1) {
            Log.i("Object3DBuilder", "Loading STL object from: $uri")
            STLLoaderTask(parent, uri, this).execute()
        } else if (uri.toString().toLowerCase().endsWith(".dae") || parent.paramType == 2) {
            Log.i("Object3DBuilder", "Loading Collada object from: $uri")
            ColladaLoaderTask(parent, uri, this).execute()
        }
    }

    private fun animateLight() {
        if (!rotatingLight) return

        // animate light - Do a complete rotation every 5 seconds.
        val time = SystemClock.uptimeMillis() % 5000L
        val angleInDegrees = 360.0f / 5000.0f * time.toInt()
        lightPoint.rotationY = angleInDegrees
    }

    @Synchronized
    internal fun addObject(obj: Object3DData) {
        val newList = ArrayList(objects)
        newList.add(obj)
        this.objects = newList
        requestRender()
    }

    private fun requestRender() {
        // request render only if GL view is already initialized
        if (parent.gLView!= null) {
            parent.gLView!!.requestRender()
        }
    }

    fun toggleWireframe() {
        if (this.drawWireframe && !this.drawingPoints) {
            this.drawWireframe = false
            this.drawingPoints = true
        } else if (this.drawingPoints) {
            this.drawingPoints = false
        } else {
            this.drawWireframe = true
        }
        requestRender()
    }

    fun toggleBoundingBox() {
        this.drawBoundingBox = !drawBoundingBox
        requestRender()
    }

    fun toggleTextures() {
        this.drawTextures = !drawTextures
    }

    fun toggleLighting() {
        if (this.drawLighting && this.rotatingLight) {
            this.rotatingLight = false
        } else if (this.drawLighting && !this.rotatingLight) {
            this.drawLighting = false
        } else {
            this.drawLighting = true
            this.rotatingLight = true
        }
        requestRender()
    }

    fun toggleAnimation() {
        if (animateModel && !drawSkeleton) {
            this.drawSkeleton = true
        } else if (animateModel) {
            this.drawSkeleton = false
            this.animateModel = false
        } else {
            animateModel = true
        }
    }

    fun toggleCollision() {
        this.isCollision = !isCollision
    }

    @Throws(IOException::class)
    fun loadTexture(obj: Object3DData?, uri: Uri) {
        var obj = obj
        if (obj == null && objects.size != 1) {
            return
        }
        obj = if (obj != null) obj else objects[0]
        obj.textureData = IOUtils.read(ContentUtils.getInputStream(uri))
        this.drawTextures = true
    }

    fun processTouch(x: Float, y: Float) {
        val mr = parent.gLView!!.mRenderer
        val objectToSelect = CollisionDetection.getBoxIntersection(objects, mr.width, mr.height, mr.modelViewMatrix, mr.modelProjectionMatrix, x, y)
        if (objectToSelect != null) {
            if (selectedObject == objectToSelect) {
                Log.i("SceneLoader", "Unselected object " + objectToSelect!!.getId())
                selectedObject = null
            } else {
                Log.i("SceneLoader", "Selected object " + objectToSelect!!.getId())
                selectedObject = objectToSelect
            }
            if (isCollision) {
                Log.d("SceneLoader", "Detecting collision...")

                val point = CollisionDetection.getTriangleIntersection(objects, mr.width, mr.height, mr.modelViewMatrix, mr.modelProjectionMatrix, x, y)
                if (point != null) {
                    Log.i("SceneLoader", "Drawing intersection point: " + Arrays.toString(point))
                    addObject(Object3DBuilder.buildPoint(point).setColor(floatArrayOf(1.0f, 0f, 0f, 1f)))
                }
            }
        }
    }

    fun processMove(dx1: Float, dy1: Float) {
        userHasInteracted = true
    }

    fun onDrawFrame() {

        animateLight()

        // smooth camera transition
        camera!!.animate()

        // initial camera animation. animate if user didn't touch the screen
        if (!userHasInteracted) {
            animateCamera()
        }

        if (objects.isEmpty()) return

        if (animateModel) {
            for (i in objects.indices) {
                val obj = objects[i]
                animator.update(obj)
            }
        }
    }



    private fun animateCamera() {
        camera!!.translateCamera(0.0025f, 0f)
    }


}