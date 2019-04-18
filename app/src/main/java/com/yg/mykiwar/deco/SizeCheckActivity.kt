package com.yg.mykiwar.deco

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
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
import com.yg.mykiwar.deco.adapter.DecoAdapter
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_size_check.*
import kotlinx.android.synthetic.main.deco_fragment.*
import java.util.*
import java.util.concurrent.CompletionException

class SizeCheckActivity : AppCompatActivity(), Scene.OnUpdateListener, Scene.OnPeekTouchListener, View.OnClickListener {


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
    private var modelRender = false

    lateinit var decoAdapter : DecoAdapter
    lateinit var requestManager : RequestManager
    lateinit var datas : ArrayList<Int>
    lateinit var onItemClick : View.OnClickListener
    var modelUri = "bighornsheep.sfb"
    var modelName = "큰뿔양"
    private var modelSelect = 0
    private var colorSelect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_size_check)
        init()
        sceneFromFragment = size_fragment as ArFragment
        sceneFromFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if(!modelRender)
                return@setOnTapArPlaneListener
            val anchor = hitResult.createAnchor()
            placeObject(sceneFromFragment, anchor, Uri.parse(modelUri), modelName)
        }
        sceneFromFragment.arSceneView.planeRenderer.isEnabled = false
        sceneFromFragment.arSceneView.scene.addOnUpdateListener(this)
        sceneFromFragment.arSceneView.scene.addOnPeekTouchListener(this)
        setUpColorPickerUi()
        //setRule()
    }

    fun init(){
        colorPickerIcon.setBackgroundColor(Color.WHITE)
        MaterialFactory.makeOpaqueWithColor(this, WHITE)
                .thenAccept { material1 -> material = material1.makeCopy() }
                .exceptionally { throwable ->
                    displayError(throwable)
                    throw CompletionException(throwable)
                }

        btn_deco_capture.setOnClickListener {

        }

        clearButton.setOnClickListener {
            for (stroke in strokes) {
                stroke.clear()
            }
            strokes.clear()
        }

        undoButton.setOnClickListener {
            if (strokes.size < 1) {
                return@setOnClickListener
            }
            val lastIndex = strokes.size - 1
            strokes[lastIndex].clear()
            strokes.removeAt(lastIndex)
        }

        btn_deco_model.setOnClickListener {
            modelRender = true
            colorSelect = 0
            colorPickerIcon.setBackgroundColor(Color.TRANSPARENT)
            btn_deco_model.setBackgroundColor(Color.WHITE)
            if(modelSelect == 0){
                //선택 팔레트 띄우지X
                modelSelect = 1
            }else{
                //선택 팔레트 띄우기
                list_deco_model.visibility = View.VISIBLE
                modelSelect = 0
            }
        }

        requestManager = Glide.with(this)
        datas = ArrayList()

        datas.add(R.drawable.bighornsheep)
        datas.add(R.drawable.buffalo)
        datas.add(R.drawable.camel)
        datas.add(R.drawable.cat)
        datas.add(R.drawable.cow)
        datas.add(R.drawable.dog)
        datas.add(R.drawable.elephant)
        datas.add(R.drawable.feret)
        datas.add(R.drawable.fox)
        datas.add(R.drawable.gazelle)
        datas.add(R.drawable.goat)
        datas.add(R.drawable.horse)
        datas.add(R.drawable.lion)
        datas.add(R.drawable.mouse)
        datas.add(R.drawable.riverotter)
        datas.add(R.drawable.panda)
        datas.add(R.drawable.penguin)
        datas.add(R.drawable.pig)
        datas.add(R.drawable.raccoon)
        datas.add(R.drawable.sheep)
        datas.add(R.drawable.snake)

        decoAdapter = DecoAdapter(datas, requestManager)
        decoAdapter.setOnItemClickListener(this)
        list_deco_model.layoutManager = GridLayoutManager(this, 3)
        list_deco_model.adapter = decoAdapter
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri, name: String) {
        if(!modelRender)
            return
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable, name) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable, name: String) {
        if(!modelRender)
            return
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
            btn_deco_model.setBackgroundColor(Color.TRANSPARENT)
            colorPickerIcon.setBackgroundColor(Color.WHITE)
            modelRender = false
            modelSelect = 0
            if(colorSelect == 0){
                //모델 누르다가 옴

                colorSelect = 1
            }else{
                //컬러 선택이 이미 한 번 됨
                if (controlsPanel.visibility == View.VISIBLE) {
                    controlsPanel.visibility = View.GONE
                    colorPanel.visibility = View.VISIBLE
                }
                colorSelect = 0
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
        Log.v("모델", modelRender.toString())
        if(modelRender)
            return
        val camera = sceneFromFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState == TrackingState.TRACKING) {
            sceneFromFragment.planeDiscoveryController.hide()
        }
    }

    override fun onPeekTouch(hitTestResult: HitTestResult?, tap: MotionEvent?) {
        Log.v("모델", modelRender.toString())
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

    override fun onClick(v: View?) {
        val idx: Int = list_deco_model!!.getChildAdapterPosition(v)
        btn_deco_model.setImageResource(datas[idx])
        modelUri = AnimalList.animalListE[idx] + ".sfb"
        modelName = AnimalList.getMatch()[AnimalList.animalList[idx]]!!
        list_deco_model.visibility = View.GONE
    }
}
