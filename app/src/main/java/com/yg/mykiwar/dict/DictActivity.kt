package com.yg.mykiwar.dict

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.yg.mykiwar.R
import com.yg.mykiwar.util.SharedPreferenceController
import kotlinx.android.synthetic.main.activity_dict.*

class DictActivity : AppCompatActivity() {
    private lateinit var arFragment : ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dict)
        arFragment = scene_dict_display as ArFragment
        val dicList = SharedPreferenceController.getDictList(this)
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            for (i in 0 until dicList.size) {
                Log.v("이름", dicList[i])
                val nameUrl = Uri.parse(dicList[i]+".sfb")
                val lineCount = dicList.size / 5 + 1
                val lineNumber = i%5
                val colNumber = i / 5
                placeObject(arFragment,
                        anchor, nameUrl, "", lineNumber, colNumber)
            }
        }
    }

    fun placeObject(arFragment : ArFragment, anchor: Anchor, model: Uri, name: String, lineCount : Int, colNumber : Int) {
        ModelRenderable.builder().setSource(arFragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(arFragment, anchor,
                        renderable, name, lineCount, colNumber) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    fun addNodeToScene(arFragment : ArFragment, anchor: Anchor, renderable: Renderable,
                       name: String, lineNumber : Int, colNumber: Int) {
        val anchorNode = AnchorNode(anchor)
        val node = Node()
        node.renderable = renderable
        node.setParent(anchorNode)
        //Vector3(0f, 0f, 0f)
        val x = (lineNumber - 2).toFloat() / 2f
        val y = colNumber.toFloat() / 2f
        val z = -0.5f
        node.localPosition = Vector3(x, y, z)
        arFragment.arSceneView.scene.addChild(anchorNode)
    }


//
//    fun getMemberList(id : String) : RealmResults<AnchorInfo> {
//        return realm.where(AnchorInfo::class.java).equalTo("id", id).findAll()
//    }
}
