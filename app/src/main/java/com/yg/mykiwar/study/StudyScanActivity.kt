package com.yg.mykiwar.study

import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.core.*
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
import java.io.ByteArrayOutputStream
import java.io.IOException

class StudyScanActivity : AppCompatActivity() {


    private lateinit var arFragment : StudyScanFragment
    private lateinit var name : String
    private lateinit var imageUrl : String
    private var takeBitmap : Bitmap? = null
    private var shouldModel = true
    private var scanLabel = ""
    private val TAG = "StudyScan"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_scan)
        FirebaseApp.initializeApp(this)
        name = intent.getStringExtra("name")
        imageUrl = "aug/" + AnimalList.getMatch()[name]+".jpeg"
        arFragment = sceneform_scan_fragment as StudyScanFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        btn_scan_check.setOnClickListener {
            scan()
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
        //FirebaseVisionImage.fromByteArray()
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
        labeler.processImage(image)
                .addOnSuccessListener { labels ->
                    for(label in labels){
                        if(label.text.toLowerCase().contains(AnimalList.getMatch()[name]!!)){
                            scanLabel = name
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.v(TAG, e.toString())
                }
    }

    fun setUpAugmentedImageDb(config : Config?, session : Session?) : Boolean{
        var bitmap : Bitmap? = loadAugmentedImage()
        return if (bitmap == null){
            Log.v("들어옴", "a")
            false
        }else{
            Log.v("들어옴", "b")
            if(takeBitmap != null)
                bitmap = takeBitmap
            val augmentedImageDatabase =  AugmentedImageDatabase(session)
            augmentedImageDatabase.addImage(name, bitmap)
            config!!.augmentedImageDatabase = augmentedImageDatabase
            true
        }
    }

    private fun loadAugmentedImage() : Bitmap?{
        //여기서 return text를 보고 판명되는 이미지가 모델에 해당하는 동물이면 이것을 띄운다.
        //if(scanLabel == AnimalList.getMatch()[name]!!.toLowerCase()){
            try {
                val ins = assets.open(imageUrl)
                Log.v("들어옴", "d")
                return BitmapFactory.decodeStream(ins)
            }catch (e : IOException){
                Log.v("들어옴", "e")
                Log.v("LOAD", e.toString())
            }
        //}
        return null
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
        if((scanLabel == name) and shouldModel){
            Log.v("메소드 캡쳐", "8")

            placeObject(arFragment, anchorNode,
                    Uri.parse(AnimalList.getMatch()[name] + ".sfb"))
            shouldModel = false
        }



//        for (augmentedImage in augmentedImages){
//            Log.v("트래킹", "1")
//            if(augmentedImage.trackingState == TrackingState.TRACKING){
//                Log.v("트래킹", "2")
//                if((augmentedImage.name == name) and shouldModel){
//                    Log.v("트래킹", "3")
//                    placeObject(arFragment, augmentedImage.createAnchor(augmentedImage.centerPose),
//                            Uri.parse(AnimalList.getMatch()[name] + ".sfb"))
//                    shouldModel = false
//                }
//            }
//        }
    }

    fun checkType(cameraImage : Image){
//The camera image received is in YUV YCbCr Format. Get buffers for each of the planes and use them to create a new bytearray defined by the size of all three buffers combined
        Log.v(TAG, "yg2")

        val cameraPlaneY = cameraImage.planes[0].buffer
        val cameraPlaneU = cameraImage.planes[1].buffer
        val cameraPlaneV = cameraImage.planes[2].buffer

//Use the buffers to create a new byteArray that
        val compositeByteArray = ByteArray(cameraPlaneY.capacity() + cameraPlaneU.capacity() + cameraPlaneV.capacity())

        cameraPlaneY.get(compositeByteArray, 0, cameraPlaneY.capacity())
        cameraPlaneU.get(compositeByteArray, cameraPlaneY.capacity(), cameraPlaneU.capacity())
        cameraPlaneV.get(compositeByteArray, cameraPlaneY.capacity() + cameraPlaneU.capacity(), cameraPlaneV.capacity())

        val baOutputStream = ByteArrayOutputStream()
        val yuvImage = YuvImage(compositeByteArray, ImageFormat.NV21, cameraImage.width, cameraImage.height, null)
        yuvImage.compressToJpeg(Rect(0, 0, cameraImage.width, cameraImage.height), 75, baOutputStream)
        val byteForBitmap = baOutputStream.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.size)
        scanner(bitmap)
    }

}
