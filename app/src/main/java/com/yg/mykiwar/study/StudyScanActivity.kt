package com.yg.mykiwar.study

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.yg.mykiwar.R
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_scan.*
import java.io.IOException

class StudyScanActivity : AppCompatActivity() {

    private lateinit var arFragment : StudyScanFragment
    lateinit var name : String
    lateinit var imageUrl : String
    var shouldModel = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_scan)
        name = intent.getStringExtra("name")
        imageUrl = "aug/" + AnimalList.getMatch()[name]+".jpeg"
        arFragment = sceneform_scan_fragment as StudyScanFragment
        //arFragment.arSceneView.arFrame.camera.
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
    }

    fun setUpAugmentedImageDb(config : Config?, session : Session?) : Boolean{
        val bitmap : Bitmap? = loadAugmentedImage()
        if (bitmap == null){
            Log.v("들어옴", "b")
            return false
        }else{
            val augmentedImageDatabase =  AugmentedImageDatabase(session)
            augmentedImageDatabase.addImage(name, bitmap)
            config!!.augmentedImageDatabase = augmentedImageDatabase
            Log.v("들어옴", "c")
            return true
        }
    }

    private fun loadAugmentedImage() : Bitmap?{
        try {
            val ins = assets.open(imageUrl)
            Log.v("들어옴", "d")
            return BitmapFactory.decodeStream(ins)
        }catch (e : IOException){
            Log.v("들어옴", "e")
            Log.v("LOAD", e.toString())
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
        val frame = arFragment.arSceneView.arFrame
        val augmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
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
}
