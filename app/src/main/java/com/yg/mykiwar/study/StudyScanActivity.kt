package com.yg.mykiwar.study

import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.wonderkiln.camerakit.*
import com.yg.mykiwar.R
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_scan.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class StudyScanActivity : AppCompatActivity() {

    private lateinit var arFragment : StudyScanFragment
    private lateinit var name : String
    private lateinit var imageUrl : String
    private var shouldModel = true
    private var scanLabel = ""
    private val TAG = "StudyScan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_scan)
        FirebaseApp.initializeApp(this)

        btn_temp_capture.setOnClickListener {
            scan_camera.start()
            scan_camera.captureImage()
        }

        scan_camera.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {
                Log.v("ygygyg", "11")
                scan_camera.stopVideo()
            }

            override fun onEvent(p0: CameraKitEvent?) {
                Log.v("ygygyg", "12")
            }

            override fun onImage(p0: CameraKitImage?) {
                Log.v("ygygyg", "13")
                var bitmap : Bitmap = p0!!.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, scan_camera.width,
                        scan_camera.height, false)
                scan_camera.stop()
                scanner(bitmap)
            }

            override fun onError(p0: CameraKitError?) {
                Log.v("ygygyg", "14")
                Log.v("ygygyg", p0!!.exception.toString())
                scan_camera.stop()
            }
        })
        initScan()
    }

    fun initScan(){
        name = intent.getStringExtra("name")
        imageUrl = "aug/" + AnimalList.getMatch()[name]+".jpeg"
        arFragment = sceneform_scan_fragment as StudyScanFragment
        //arFragment.arSceneView.arFrame.camera.
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
    }

    private fun scanner(bitmap : Bitmap) {
        Log.v(TAG, "yg3")
        //FirebaseVisionImage.fromByteArray()
        val image = FirebaseVisionImage.fromBitmap(bitmap)
         val options = FirebaseVisionCloudImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)
        labeler.processImage(image)
                .addOnSuccessListener { labels ->
                    for(label in labels){
                        Log.v(TAG, "yg4")
                        scanLabel = label.text
                        Log.v(TAG, "yg " + label.text)
                    }
                }
                .addOnFailureListener { e ->
                    Log.v(TAG, "yg5")
                    Log.v(TAG, e.toString())
                }

    }

    fun setUpAugmentedImageDb(config : Config?, session : Session?) : Boolean{
        val bitmap : Bitmap? = loadAugmentedImage()
        if (bitmap == null){
            return false
        }else{
            val augmentedImageDatabase =  AugmentedImageDatabase(session)
            augmentedImageDatabase.addImage(name, bitmap)
            config!!.augmentedImageDatabase = augmentedImageDatabase
            return true
        }
    }

    private fun loadAugmentedImage() : Bitmap?{
        //여기서 return text를 보고 판명되는 이미지가 모델에 해당하는 동물이면 이것을 띄운다.
        if(scanLabel == AnimalList.getMatch()[name]!!.toLowerCase()){
            try {
                val ins = assets.open(imageUrl)
                Log.v("들어옴", "d")
                return BitmapFactory.decodeStream(ins)
            }catch (e : IOException){
                Log.v("들어옴", "e")
                Log.v("LOAD", e.toString())

            }
        }
        return null
    }

    private fun placeObject(fragment : ArFragment, anchor: Anchor, model : Uri){
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable){
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    fun onUpdateFrame(frameTime: FrameTime){
        try{
            val captureImage = arFragment.arSceneView.arFrame!!.acquireCameraImage()
            checkType(captureImage)
        }catch (e : Exception){
            Log.v(TAG, e.toString())
        }
        val frame = arFragment.arSceneView.arFrame
        Log.v(TAG, "yg1")
        //frame.imageMetadata.getByteArray()
        val augmentedImages = frame!!.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in augmentedImages){
            if(augmentedImage.trackingState == TrackingState.TRACKING){
                if((augmentedImage.name == name) and shouldModel){
                    placeObject(arFragment, augmentedImage.createAnchor(augmentedImage.centerPose),
                            Uri.parse(AnimalList.getMatch()[name] + ".sfb"))
                    shouldModel = false
                }
            }
        }
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
