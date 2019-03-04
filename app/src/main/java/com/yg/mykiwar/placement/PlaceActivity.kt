package com.yg.mykiwar.placement

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import com.yg.mykiwar.util.SharedPreferenceController
import com.yg.mykiwar.util.helper.SnackbarHelper
import kotlinx.android.synthetic.main.activity_place.*




class PlaceActivity : AppCompatActivity() {

    private var cloudAnchor : Anchor? = null
    lateinit var fragment : PlaceFragment
    var snackbarHelper = SnackbarHelper()
    var placeAnchorState = PlaceAnchorState.HOSTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        setNewCloudAnchor(null)

        fragment = fragment_place as PlaceFragment
        fragment.planeDiscoveryController.hide()
        fragment.arSceneView.scene.addOnUpdateListener(this::onUpadteFrame)

        fragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if((plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) or (placeAnchorState != PlaceAnchorState.NONE))
                return@setOnTapArPlaneListener
//            val newAnchor = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(hitResult.createAnchor())
            setNewCloudAnchor(newAnchor)
            placeAnchorState = PlaceAnchorState.HOSTING
            snackbarHelper.showMessage(this, "Now hosting anchor...")

            placeObject(fragment, cloudAnchor, Uri.parse("penguin.sfb"))
        }

        btn_place_clean.setOnClickListener {
            setNewCloudAnchor(null)
        }

        btn_place_resolve.setOnClickListener {
            val cloudAnchorId = SharedPreferenceController.getId(this, "P")
            val resolvedAnchor = fragment.arSceneView.session.resolveCloudAnchor(cloudAnchorId)
            setNewCloudAnchor(resolvedAnchor)
            placeObject(fragment, cloudAnchor, Uri.parse("penguin.sfb"))
            snackbarHelper.showMessage(this, "Now Resolving Anchor")
            placeAnchorState = PlaceAnchorState.RESOLVING
        }

    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor?, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept{ renderable -> addNodeToScene(fragment, anchor, renderable) }
                .exceptionally{ throwable ->
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(throwable.message)
                            .setTitle("Error!")
                    val dialog = builder.create()
                    dialog.show()
                    null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor?, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    fun setNewCloudAnchor(newAnchor: Anchor?){
        if(cloudAnchor != null)
            cloudAnchor!!.detach()

        cloudAnchor = newAnchor
        placeAnchorState = PlaceAnchorState.NONE
        snackbarHelper.hide(this)
    }

    fun onUpadteFrame(frameTime: FrameTime){
        checkUpdateAnchor()


    }

    @Synchronized
    fun checkUpdateAnchor(){
        if((placeAnchorState != PlaceAnchorState.HOSTING) and (placeAnchorState != PlaceAnchorState.RESOLVING)){
            return
        }
        val cloudState = cloudAnchor!!.cloudAnchorState

        if(placeAnchorState == PlaceAnchorState.HOSTING) {
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor... ${cloudState}")
                placeAnchorState = PlaceAnchorState.NONE
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                SharedPreferenceController.setId(this, cloudAnchor!!.cloudAnchorId, "P")
                snackbarHelper.showMessageWithDismiss(this, "success... ${cloudAnchor!!.cloudAnchorId}")
                placeAnchorState = PlaceAnchorState.HOSTED
            }
        }else if(placeAnchorState == PlaceAnchorState.RESOLVING){
            if (cloudState.isError) {
                snackbarHelper.showMessageWithDismiss(this, "Error resloving anchor... ${cloudState}")
                placeAnchorState = PlaceAnchorState.NONE
                Log.v("error",cloudState.toString())
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                //SharedPreferenceController.setId(this, cloudAnchor!!.cloudAnchorId, "P")
                snackbarHelper.showMessageWithDismiss(this, "success... ${cloudAnchor!!.cloudAnchorId}")
                placeAnchorState = PlaceAnchorState.RESOLVED
            }
        }
    }


}
