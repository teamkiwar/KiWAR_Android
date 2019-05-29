package com.yg.mykiwar.play.catchgame

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import com.yg.mykiwar.play.PlayFragment
import com.yg.mykiwar.play.catchgame.adapter.PlayCatchAdapter
import com.yg.mykiwar.util.AnimalList
import com.yg.mykiwar.util.CommonData
import com.yg.mykiwar.util.CustomDialog
import com.yg.mykiwar.util.SharedPreferenceController
import kotlinx.android.synthetic.main.activity_catch.*
import java.util.*
import kotlin.collections.ArrayList

class CatchActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var playCatchAdapter: PlayCatchAdapter
    private lateinit var answerList: ArrayList<String>
    private lateinit var answer: String
    private lateinit var anchorNode: AnchorNode
    private lateinit var selectedNode: Node
    private lateinit var sceneFromFragment : PlayFragment
    private var isLoaded : Boolean = false
    private var answerCount = 0

    var setting = false
    val request = 1001

    private val logoutCustomDialog: CustomDialog  by lazy {
        CustomDialog(this@CatchActivity, "로그인이 필요한 서비스입니다.\n로그인 하시겠습니까?", responseRight, responseLeft, "취소", "확인")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catch)
        sceneFromFragment = catch_sceneform_fragment as PlayFragment
        //val image = sceneFromFragment.arSceneView.arFrame!!.acquireCameraImage()

        sceneFromFragment.planeDiscoveryController.hide()
        sceneFromFragment.arSceneView.scene.addOnUpdateListener(this::init)
        tv_catch_answer_count.text = answerCount.toString() + " / 10"
//        sceneFromFragment.setOnTapArPlaneListener { hitResult, _, _ ->
////            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
////                return@setOnTapArPlaneListener
////            }
//            CommonData.anchor = anchor
//
//            //흰색 땡땡이를 찾으면 게임이 시작돼요! 라는 문구 넣기.
//            val cameraPos = sceneFromFragment.arSceneView.scene.camera.worldPosition
//            val cameraForward = sceneFromFragment.arSceneView.scene.camera.forward
//            val position = Vector3.add(cameraPos, cameraForward.scaled(1.0f))
//            val pose = Pose.makeTranslation(position.x, position.y, position.z)
//            val anchor = sceneFromFragment.arSceneView.session!!.createAnchor(pose)
//            for (i in 0..10) {
//                val name = AnimalList.animalList[Random().nextInt(21)]
//                val nameUrl = Uri.parse(AnimalList.getMatch()[name]+".sfb")
//                placeObject(sceneFromFragment, anchor, nameUrl, name)
//            }
//            sceneFromFragment.setOnTapArPlaneListener(null)
//        }
        btn_catch_back.setOnClickListener {
            finish()
        }

        frame_catch_list.setOnClickListener {
            frame_catch_list.visibility = View.GONE
        }
    }

    private fun init(frameTime: FrameTime){
        if (sceneFromFragment.arSceneView.arFrame!!.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        if(!isLoaded){
            isLoaded = true
            sceneFromFragment.arSceneView.scene.removeOnUpdateListener(this::init)
            val cameraPos = sceneFromFragment.arSceneView.scene.camera.worldPosition
            val cameraForward = sceneFromFragment.arSceneView.scene.camera.forward
            val position = Vector3.add(cameraPos, cameraForward.scaled(1.0f))
            val pose = Pose.makeTranslation(position.x, position.y, position.z)
            val anchor = sceneFromFragment.arSceneView.session!!.createAnchor(pose)
            for (i in 0..10) {
                val name = AnimalList.animalList[Random().nextInt(21)]
                val nameUrl = Uri.parse(AnimalList.getMatch()[name]+".sfb")
                placeObject(sceneFromFragment, anchor, nameUrl, name)
            }
        }
    }

    fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri, name: String) {
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable, name) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable, name: String) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        //Vector3(0f, 0f, 0f)
        val x = Random().nextInt(15) / 10f - Random().nextInt(1) / 10f
        val y = Random().nextInt(15) / 10f
        val z = -(Random().nextInt(15) / 10f)
        node.localPosition = Vector3(x, y, z)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.setOnTapListener { _, _ ->
            Log.v("탭", name)
            frame_catch_list.visibility = View.VISIBLE
            answer = name
            setAnswerList(name)
            this.anchorNode = anchorNode
            selectedNode = node
            node.select()

        }
    }

    fun setAnswerList(name: String) {
        answerList = ArrayList()
        var count = 0
        while (true) {
            val candidate = AnimalList.animalList[Random().nextInt(AnimalList.animalList.size)]
            if (answerList.contains(candidate) or (name == candidate))
                continue
            answerList.add(candidate)
            count++
            if (count == 2) {
                answerList.add(name)
                answerList.sort()
                break
            }
        }
        playCatchAdapter = PlayCatchAdapter(answerList)
        playCatchAdapter.setOnItemClickListener(this)
        rv_play_catch_list.layoutManager = LinearLayoutManager(this)
        rv_play_catch_list.adapter = playCatchAdapter
    }

    private val responseRight = View.OnClickListener {

        logoutCustomDialog.dismiss()
    }
    private val responseLeft = View.OnClickListener {
        logoutCustomDialog.dismiss()
        //##로그아웃
    }

    override fun onClick(v: View?) {
        val idx: Int = rv_play_catch_list!!.getChildAdapterPosition(v)
        val name: String? = answerList[idx]
        if (name == answer) {
            //name은 한글
            anchorNode.removeChild(selectedNode)
            Toast.makeText(this, "정답입니다.", Toast.LENGTH_SHORT).show()
            CommonData.dictList.add(AnimalList.getMatch()[name]!!)
            //도감용 저장은 영어이름으로 할 것.
            SharedPreferenceController.setDictList(this, CommonData.dictList)
            //이거도 저장은 영어.
            answerCount++
            tv_catch_answer_count.text = answerCount.toString() + " / 10"
            if(answerCount == 10){
                Toast.makeText(this, "게임이 끝났습니다.", Toast.LENGTH_SHORT).show()
                Thread.sleep(500)
                finish()
            }
        } else
            Toast.makeText(this, "다시 생각해보세요.", Toast.LENGTH_SHORT).show()
        frame_catch_list.visibility = View.GONE
    }


}
