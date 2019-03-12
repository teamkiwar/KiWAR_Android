package com.yg.mykiwar.play.findgame

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.activity_find.*
import java.util.*



class FindActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)


        val sceneFromFragment = find_sceneform_fragment as ArFragment

        sceneFromFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val quizAnchor = hitResult.createAnchor()
            placeQuiz(sceneFromFragment, quizAnchor, "펭귄")

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
    }

    val placeQuiz = {fragment : ArFragment, anchor : Anchor, quiz : String ->
        if (tv_find_quiz.parent != null) {
            (tv_find_quiz.parent as ViewGroup).removeView(tv_find_quiz) // <- fix
        }
        //find_layout.addView(tv_find_quiz)
        tv_find_quiz.text = quiz
        ViewRenderable.builder()
                .setView(this, tv_find_quiz)
                .build()
                .thenAccept { renderable -> addAnswerToScene(fragment, anchor, renderable) }
                .exceptionally {
                    Toast.makeText(this, "Unable to load view renderable", Toast.LENGTH_SHORT).show()
                    null
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

    val addAnswerToScene = {fragment: ArFragment, anchor: Anchor, renderable: Renderable-> val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
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
//            frame_catch_list.visibility = View.VISIBLE
//            answer = name
//            setAnswerList(name)
//            this.anchorNode = anchorNode
//            selectedNode = node
            if (name == "펭귄") {
                anchorNode.removeChild(node)
                Toast.makeText(this, "정답입니다.", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this, "다시 생각해보세요.", Toast.LENGTH_SHORT).show()
            node.select()
        }
    }
}
