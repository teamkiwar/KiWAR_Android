package com.yg.mykiwar.play.catchgame

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import com.yg.mykiwar.play.catchgame.adapter.PlayCatchAdapter
import com.yg.mykiwar.util.AnimalList
import com.yg.mykiwar.util.CustomDialog
import kotlinx.android.synthetic.main.activity_catch.*
import java.util.*
import kotlin.collections.ArrayList

class CatchActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var playCatchAdapter: PlayCatchAdapter
    private lateinit var answerList: ArrayList<String>
    private lateinit var answer: String
    private lateinit var anchorNode: AnchorNode
    private lateinit var selectedNode: Node
    var setting = false

    private val logoutCustomDialog: CustomDialog  by lazy {
        CustomDialog(this@CatchActivity, "로그인이 필요한 서비스입니다.\n로그인 하시겠습니까?", responseRight, responseLeft, "취소", "확인")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catch)

        val sceneFromFragment = catch_sceneform_fragment as ArFragment

        sceneFromFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            for (i in 0..15) {
                when (i / 5) {
                    0 -> {
                        placeObject(sceneFromFragment, anchor, Uri.parse("cat.sfb"), "고양이")
                    }
                    1 -> {
                        placeObject(sceneFromFragment, anchor, Uri.parse("penguin.sfb"), "펭귄")
                    }
                    2 -> {
                        placeObject(sceneFromFragment, anchor, Uri.parse("dog.sfb"), "개")
                    }
                }
            }
            sceneFromFragment.setOnTapArPlaneListener(null)
        }

        frame_catch_list.setOnClickListener {
            frame_catch_list.visibility = View.GONE
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
        Vector3(0.1f, 0.1f, 0.1f)
        val x = Random().nextInt(10) / 10f
        val y = Random().nextInt(10) / 10f
        val z = Random().nextInt(10) / 10f
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
            anchorNode.removeChild(selectedNode)
            Toast.makeText(this, "정답입니다.", Toast.LENGTH_SHORT).show()
        } else
            Toast.makeText(this, "다시 생각해보세요.", Toast.LENGTH_SHORT).show()
        frame_catch_list.visibility = View.GONE
    }
}
