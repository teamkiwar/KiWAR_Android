package com.yg.mykiwar.study

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.yg.mykiwar.R
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_scan.*
import java.io.IOException

class StudyScanActivity : AppCompatActivity() {

    private lateinit var arFragment : StudyScanFragment
    private lateinit var name : String
    private lateinit var imageUrl : String
    private var takeBitmap : Bitmap? = null
    private var shouldModel = false
    private var scanLabel = ""
    private val TAG = "StudyScan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_scan)
        FirebaseApp.initializeApp(this)
        arFragment = sceneform_scan_fragment as StudyScanFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        btn_scan_check.setOnClickListener {
            image_scan_state.visibility = View.VISIBLE
            val slowlyDisappear = AnimationUtils.loadAnimation(this,R.anim.fade_out)
            image_scan_state.animation = slowlyDisappear
            scan()
            val handler = Handler()
            handler.postDelayed(Runnable {
                image_scan_state.visibility = View.GONE
            }, 500)
        }

        btn_scan_back.setOnClickListener {
            finish()
        }
    }

    private fun scan(){
        val view = arFragment.arSceneView
        takeBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        PixelCopy.request(view, takeBitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    scanner(takeBitmap!!)
                    Log.v("메소드 캡쳐", "1")
                } catch (e: IOException) {
                    Log.v("메소드 캡쳐", "2")
                    return@request
                }
            } else {
            }
            Log.v("메소드 캡쳐", "3")

            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
        Log.v("메소드 캡쳐", "4")

    }

    private fun scanner(bitmap : Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
        labeler.processImage(image)
                .addOnSuccessListener { labels ->
                    for(label in labels){
                        name = label.text.toLowerCase()
                        Log.v("동물", name)
                        for (animal in AnimalList.animalListE){
                            if(name.contains(animal)){
                                scanLabel = animal
                                shouldModel = true
                                return@addOnSuccessListener
                            }
                        }
                    }
                    Toast.makeText(applicationContext, "키워에서 찾지 못했어요.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.v(TAG, e.toString())
                    Toast.makeText(applicationContext, "키워에서 찾지 못했어요.", Toast.LENGTH_SHORT).show()
                }
    }

    private fun placeObject(fragment : ArFragment, anchor: AnchorNode, model : Uri){
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    Log.v("메소드 캡쳐", "5")
                    null
                }
    }

    fun addNodeToScene(fragment: ArFragment, anchor: AnchorNode, renderable: Renderable){
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchor)
        fragment.arSceneView.scene.addChild(anchor)
        node.select()
    }

    private fun onUpdateFrame(frameTime: FrameTime){
        val frame = arFragment.arSceneView.arFrame
        //val augmentedImages = frame!!.getUpdatedTrackables(AugmentedImage::class.java)
        Log.v("트래킹", "6")
        if (arFragment.arSceneView.arFrame!!.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        val cameraPos = arFragment.arSceneView.scene.camera.worldPosition
        val cameraForward = arFragment.arSceneView.scene.camera.forward
        val position = Vector3.add(cameraPos, cameraForward.scaled(1.0f))
        val pose = Pose.makeTranslation(position.x, position.y, position.z)
        Log.v("메소드 캡쳐", "7")

        val anchorNode = AnchorNode(arFragment.arSceneView.session!!.createAnchor(pose))
        if(shouldModel){
            placeObject(arFragment, anchorNode,
                    Uri.parse(scanLabel + ".sfb"))
            Toast.makeText(this, AnimalList.animalList[AnimalList.animalListE.indexOf(scanLabel)]
                    + "입니다!",
                    Toast.LENGTH_SHORT).show()
            shouldModel = false
        }
    }
}
