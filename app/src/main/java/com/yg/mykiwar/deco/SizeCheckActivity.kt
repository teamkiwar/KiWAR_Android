package com.yg.mykiwar.deco

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.activity_size_check.*

class SizeCheckActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_size_check)

        val sceneFromFragment = size_fragment as ArFragment
        sceneFromFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
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
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }
}
