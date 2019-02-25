package com.yg.mykiwar.study.card

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.yg.engine.android_3d_model_engine.animation.Animator
import com.yg.engine.android_3d_model_engine.drawer.DrawerFactory
import com.yg.engine.android_3d_model_engine.drawer.Object3DImpl
import com.yg.engine.android_3d_model_engine.model.AnimatedModel
import com.yg.engine.android_3d_model_engine.model.Object3DData
import com.yg.engine.android_3d_model_engine.services.Object3DBuilder
import com.yg.engine.util.android.GLUtil
import com.yg.mykiwar.study.model.StudyModelRender
import java.io.ByteArrayInputStream
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class StudyCardRenderer(var main: StudyCardSurfaceView) : GLSurfaceView.Renderer {

    private val TAG = StudyModelRender::class.java!!.getName()

    // 3D window (parent component)
    // width of the screen
    var width: Int = 0
    // height of the screen
    var height: Int = 0
    // frustrum - nearest pixel
    val near = 1f
    // frustrum - fartest pixel
    val far = 100f

    private var drawer: DrawerFactory? = null
    // The wireframe associated shape (it should be made of lines only)
    private val wireframes = HashMap<Object3DData, Object3DData>()
    // The loaded textures
    private val textures = HashMap<ByteArray, Int>()
    // The corresponding opengl bounding boxes and drawer
    private val boundingBoxes = HashMap<Object3DData, Object3DData>()
    // The corresponding opengl bounding boxes
    private val normals = HashMap<Object3DData, Object3DData>()
    private val skeleton = HashMap<Object3DData, Object3DData>()

    // 3D matrices to project our 3D world
    var modelProjectionMatrix = FloatArray(16)
    var modelViewMatrix = FloatArray(16)
    // mvpMatrix is an abbreviation for "Model View Projection Matrix"
    private val mvpMatrix = FloatArray(16)

    // light position required to render with lighting
    private val lightPosInEyeSpace = FloatArray(4)
    /**
     * Whether the info of the model has been written to console log
     */
    private var infoLogged = false

    /**
     * Skeleton Animator
     */
    private val animator = Animator()

    /**
     * Construct a new renderer for the specified surface view
     *
     * @param modelSurfaceView
     * the 3D window
     */


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        var scene : StudyCardSceneLoader? = main.parent.scene

        if(scene == null)
            return

        scene.onDrawFrame()

        val camera = scene.camera
        if (camera!!.hasChanged()) {
            Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                    camera.zView, camera.xUp, camera.yUp, camera.zUp)
            // Log.d("Camera", "Changed! :"+camera.ToStringVector());
            Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0)
            camera.setChanged(false)
        }

        if (scene.drawLighting) {

            val lightBulbDrawer = drawer!!.getPointDrawer() as Object3DImpl

            val lightModelViewMatrix = lightBulbDrawer.getMvMatrix(lightBulbDrawer.getMMatrix(scene.lightPoint), modelViewMatrix)

            // Calculate position of the light in eye space to support lighting
            Matrix.multiplyMV(lightPosInEyeSpace, 0, lightModelViewMatrix, 0, scene.lightPosition, 0)

            // Draw a point that represents the light bulb
            lightBulbDrawer.draw(scene.lightPoint, modelProjectionMatrix, modelViewMatrix, -1, lightPosInEyeSpace)
        }


        val objects = scene.objects

        for (i in objects.indices) {
            var objData: Object3DData? = null
            try {
                objData = objects.get(i)
                val changed = objData!!.isChanged

                var drawerObject = drawer!!.getDrawer(objData, scene.drawTextures, scene.drawLighting,
                        scene.animateModel)

                if (!infoLogged) {
                    Log.i("ModelRenderer", "Using drawer " + drawerObject::class.java)
                    infoLogged = true
                }

                var textureId: Int? = textures[objData.textureData]
                if (textureId == null && objData.textureData != null) {
                    Log.i("ModelRenderer", "Loading GL Texture...")
                    val textureIs = ByteArrayInputStream(objData.textureData)
                    textureId = GLUtil.loadTexture(textureIs)
                    textureIs.close()
                    textures[objData.textureData] = textureId
                }

                if (objData.drawMode == GLES20.GL_POINTS) {
                    val lightBulbDrawer = drawer!!.getPointDrawer() as Object3DImpl
                    lightBulbDrawer.draw(objData, modelProjectionMatrix, modelViewMatrix, GLES20.GL_POINTS, lightPosInEyeSpace)
                } else if (scene.isAnaglyph) {
                    // TODO: implement anaglyph
                } else if (scene.drawWireframe && objData.drawMode != GLES20.GL_POINTS
                        && objData.drawMode != GLES20.GL_LINES && objData.drawMode != GLES20.GL_LINE_STRIP
                        && objData.drawMode != GLES20.GL_LINE_LOOP) {
                    // Log.d("ModelRenderer","Drawing wireframe model...");
                    try {
                        // Only draw wireframes for objects having faces (triangles)
                        var wireframe: Object3DData? = wireframes[objData]
                        if (wireframe == null || changed) {
                            Log.i("ModelRenderer", "Generating wireframe model...")
                            wireframe = Object3DBuilder.buildWireframe(objData)
                            wireframes[objData] = wireframe
                        }
                        drawerObject.draw(wireframe, modelProjectionMatrix, modelViewMatrix, wireframe!!.drawMode,
                                wireframe.drawSize, if (textureId != null) textureId else -1, lightPosInEyeSpace)
                    } catch (e: Error) {
                        Log.e("ModelRenderer", e.message, e)
                    }

                } else if (scene.drawingPoints || objData.faces == null || !objData.faces.loaded()) {
                    drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix, GLES20.GL_POINTS, objData.drawSize,
                            if (textureId != null) textureId else -1, lightPosInEyeSpace)
                } else if (scene.drawSkeleton && objData is AnimatedModel && (objData)
                                .getAnimation() != null) {
                    var skeleton: Object3DData? = this.skeleton[objData]
                    if (skeleton == null) {
                        skeleton = Object3DBuilder.buildSkeleton(objData as AnimatedModel?)
                        this.skeleton[objData] = skeleton
                    }
                    animator.update(skeleton)
                    drawerObject = drawer!!.getDrawer(skeleton, false, scene.drawLighting, scene
                            .animateModel)
                    drawerObject.draw(skeleton, modelProjectionMatrix, modelViewMatrix, -1, lightPosInEyeSpace)
                } else {
                    drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix,
                            if (textureId != null) textureId else -1, lightPosInEyeSpace)
                }

                // Draw bounding box
                if (scene.drawBoundingBox || scene.selectedObject == objData) {
                    var boundingBoxData: Object3DData? = boundingBoxes[objData]
                    if (boundingBoxData == null || changed) {
                        boundingBoxData = Object3DBuilder.buildBoundingBox(objData)
                        boundingBoxes[objData] = boundingBoxData
                    }
                    val boundingBoxDrawer = drawer!!.getBoundingBoxDrawer()
                    boundingBoxDrawer.draw(boundingBoxData, modelProjectionMatrix, modelViewMatrix, -1, null)
                }

                // Draw normals
                if (scene.drawNormals) {
                    var normalData: Object3DData? = normals[objData]
                    if (normalData == null || changed) {
                        normalData = Object3DBuilder.buildFaceNormals(objData)
                        if (normalData != null) {
                            // it can be null if object isnt made of triangles
                            normals[objData] = normalData
                        }
                    }
                    if (normalData != null) {
                        val normalsDrawer = drawer!!.getFaceNormalsDrawer()
                        normalsDrawer.draw(normalData, modelProjectionMatrix, modelViewMatrix, -1, null)
                    }
                }
                // TODO: enable this only when user wants it
                // obj3D.drawVectorNormals(result, modelViewMatrix);
            } catch (ex: Exception) {
                Log.e("ModelRenderer", "There was a problem rendering the object '" + objData!!.id + "':" + ex.message, ex)
            }

        }


    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height

        // Adjust the viewport based on geometry changes, such as screen rotation
        GLES20.glViewport(0, 0, width, height)

        // INFO: Set the camera position (View matrix)
        // The camera has 3 vectors (the position, the vector where we are looking at, and the up position (sky)
        val scene = main.parent.scene
        val camera = scene!!.camera

        Matrix.setLookAtM(modelViewMatrix, 0, camera!!.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                camera.zView, camera.xUp, camera.yUp, camera.zUp)

        // the projection matrix is the 3D virtual space (cube) that we want to project
        val ratio = width.toFloat() / height
        Log.d(TAG, "projection: [" + -ratio + "," + ratio + ",-1,1]-near/far[1,10]")
        Matrix.frustumM(modelProjectionMatrix, 0, -ratio, ratio, -1f, 1f, near, far)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        //GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3])

        // Use culling to remove back faces.
        // Don't remove back faces so we can see them
        // GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing for hidden-surface elimination.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Enable blending for combining colors when there is transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // This component will draw the actual models using OpenGL
        drawer = DrawerFactory()

    }
}