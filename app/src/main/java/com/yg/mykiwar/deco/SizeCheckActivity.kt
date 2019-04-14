package com.yg.mykiwar.deco

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.activity_size_check.*
import java.util.*
import java.util.concurrent.CompletionException

class SizeCheckActivity : AppCompatActivity(), Scene.OnUpdateListener, Scene.OnPeekTouchListener {

    private val TAG = SizeCheckActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0
    private val DRAW_DISTANCE = 0.13f
    private val WHITE = com.google.ar.sceneform.rendering.Color(Color.WHITE)
    private val RED = com.google.ar.sceneform.rendering.Color(Color.RED)
    private val GREEN = com.google.ar.sceneform.rendering.Color(Color.GREEN)
    private val BLUE = com.google.ar.sceneform.rendering.Color(Color.BLUE)
    private val BLACK = com.google.ar.sceneform.rendering.Color(Color.BLACK)

    lateinit var sceneFromFragment: ArFragment
    private var anchorNode: AnchorNode? = null
    private val strokes = ArrayList<Stroke>()
    private var material: Material? = null
    private var currentStroke: Stroke? = null
    var modelRender = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_size_check)

        MaterialFactory.makeOpaqueWithColor(this, WHITE)
                .thenAccept { material1 -> material = material1.makeCopy() }
                .exceptionally { throwable ->
                    displayError(throwable)
                    throw CompletionException(throwable)
                }

        clearButton.setOnClickListener {
            for (stroke in strokes) {
                stroke.clear()
            }
            strokes.clear()
        }

        btn_deco_change.setOnClickListener {
            modelRender = !modelRender
//            modelRender = if(modelRender) {
//                //모델 그리기 모드에서 버튼 클릭
//                sceneFromFragment.arSceneView.scene.addOnUpdateListener(this)
//                sceneFromFragment.arSceneView.scene.addOnPeekTouchListener(this)
//                false
//            }else {
//                //그림 그리기 모드에서 버튼 클릭
//                sceneFromFragment.arSceneView.scene.addOnUpdateListener(null)
//                sceneFromFragment.arSceneView.scene.addOnPeekTouchListener(null)
//                true
//            }
        }

        undoButton.setOnClickListener {
            if (strokes.size < 1) {
                return@setOnClickListener
            }
            val lastIndex = strokes.size - 1
            strokes[lastIndex].clear()
            strokes.removeAt(lastIndex)
        }
        sceneFromFragment = size_fragment as ArFragment
        sceneFromFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if(!modelRender)
                return@setOnTapArPlaneListener
            val anchor = hitResult.createAnchor()
            when (intent.getIntExtra("name", 0)) {
                1 -> placeObject(sceneFromFragment, anchor, Uri.parse("bighornsheep.sfb"), "고양이")
                //왕큼
                2 -> placeObject(sceneFromFragment, anchor, Uri.parse("buffalo.sfb"), "고양이")
                //큼
                3 -> placeObject(sceneFromFragment, anchor, Uri.parse("camel.sfb"), "고양이")
                //큼
                4 -> placeObject(sceneFromFragment, anchor, Uri.parse("cow.sfb"), "고양이")
                //왕큼
                5 -> placeObject(sceneFromFragment, anchor, Uri.parse("dog.sfb"), "고양이")
                //큼
                6 -> placeObject(sceneFromFragment, anchor, Uri.parse("elephant.sfb"), "고양이")
                //큼
                7 -> placeObject(sceneFromFragment, anchor, Uri.parse("ferret.sfb"), "고양이")
                //큼
                8 -> placeObject(sceneFromFragment, anchor, Uri.parse("fox.sfb"), "고양이")
                //큼
                9 -> placeObject(sceneFromFragment, anchor, Uri.parse("gazelle.sfb"), "고양이")
                //큼
                10 -> placeObject(sceneFromFragment, anchor, Uri.parse("goat.sfb"), "고양이")
                //큼
                11 -> placeObject(sceneFromFragment, anchor, Uri.parse("horse.sfb"), "고양이")
                //
                12 -> placeObject(sceneFromFragment, anchor, Uri.parse("lion.sfb"), "고양이")
                13 -> placeObject(sceneFromFragment, anchor, Uri.parse("mouse.sfb"), "고양이")
                14 -> placeObject(sceneFromFragment, anchor, Uri.parse("panda.sfb"), "고양이")
                15 -> placeObject(sceneFromFragment, anchor, Uri.parse("pig.sfb"), "고양이")
                16 -> placeObject(sceneFromFragment, anchor, Uri.parse("raccoon.sfb"), "고양이")
                17 -> placeObject(sceneFromFragment, anchor, Uri.parse("riverotter.sfb"), "고양이")
                18 -> placeObject(sceneFromFragment, anchor, Uri.parse("sheep.sfb"), "고양이")
                19 -> placeObject(sceneFromFragment, anchor, Uri.parse("snake.sfb"), "고양이")

            }
        }

        sceneFromFragment.arSceneView.planeRenderer.isEnabled = false
        sceneFromFragment.arSceneView.scene.addOnUpdateListener(this)
        sceneFromFragment.arSceneView.scene.addOnPeekTouchListener(this)
        setUpColorPickerUi()
        //setRule()
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri, name: String) {
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable, name) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable, name: String) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private fun setUpColorPickerUi() {
        colorPanel.visibility = View.GONE
        colorPickerIcon.setOnClickListener {
            if (controlsPanel.visibility == View.VISIBLE) {
                controlsPanel.visibility = View.GONE
                colorPanel.visibility = View.VISIBLE
            }
        }

        whiteCircle.setOnClickListener {
            setColor(WHITE)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_white)
        }

        redCircle.setOnClickListener {
            setColor(RED)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_red)
        }

        blackCircle.setOnClickListener {
            setColor(BLACK)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_black)
        }

        blueCircle.setOnClickListener {
            setColor(BLUE)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_blue)
        }

        greenCircle.setOnClickListener {
            setColor(GREEN)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_green)
        }

        rainbowCircle.setOnClickListener {
            setTexture(R.drawable.rainbow_texture)
            colorPickerIcon.setImageResource(R.drawable.ic_selected_rainbow)
        }
    }

    private fun setTexture(resourceId: Int) {
        Texture.builder()
                .setSource(sceneFromFragment.context!!, resourceId)
                .setSampler(Texture.Sampler.builder().setWrapMode(Texture.Sampler.WrapMode.REPEAT).build())
                .build()
                .thenCompose { texture -> MaterialFactory.makeOpaqueWithTexture(sceneFromFragment.context!!, texture) }
                .thenAccept { material1 -> material = material1.makeCopy() }
                .exceptionally { throwable ->
                    displayError(throwable)
                    throw CompletionException(throwable)
                }

        colorPanel.visibility = View.GONE
        controlsPanel.visibility = View.VISIBLE
    }


    private fun setColor(color: com.google.ar.sceneform.rendering.Color) {
        MaterialFactory.makeOpaqueWithColor(sceneFromFragment.context!!, color)
                .thenAccept { material1 -> material = material1.makeCopy() }
                .exceptionally { throwable ->
                    displayError(throwable)
                    throw CompletionException(throwable)
                }
        colorPanel.visibility = View.GONE
        controlsPanel.visibility = View.VISIBLE
    }

    private fun displayError(throwable: Throwable) {
        Log.e(TAG, "Unable to create material", throwable)
        val toast = Toast.makeText(this, "Unable to create material", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }


    override fun onUpdate(frameTime: FrameTime?) {
        if(modelRender)
            return
        val camera = sceneFromFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState == TrackingState.TRACKING) {
            sceneFromFragment.planeDiscoveryController.hide()
        }
    }

    override fun onPeekTouch(hitTestResult: HitTestResult?, tap: MotionEvent?) {
        if(modelRender)
            return
        val action = tap!!.action
        val camera = sceneFromFragment.arSceneView.scene.camera
        val ray = camera.screenPointToRay(tap.x, tap.y)
        val drawPoint = ray.getPoint(DRAW_DISTANCE)
        if (action == MotionEvent.ACTION_DOWN) {
            if (anchorNode == null) {
                val arSceneView = sceneFromFragment.arSceneView
                val coreCamera = arSceneView.arFrame!!.camera
                if (coreCamera.trackingState != TrackingState.TRACKING) {
                    return
                }
                val pose = coreCamera.pose
                anchorNode = AnchorNode(arSceneView.session!!.createAnchor(pose))
                anchorNode!!.setParent(arSceneView.scene)
            }
            currentStroke = Stroke(anchorNode, material)
            strokes.add(currentStroke!!)
            currentStroke!!.add(drawPoint)
        } else if (action == MotionEvent.ACTION_MOVE && currentStroke != null) {
            currentStroke!!.add(drawPoint)
        }
    }
}
