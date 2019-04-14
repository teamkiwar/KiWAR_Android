package com.yg.mykiwar.dict

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.yg.mykiwar.R
import com.yg.mykiwar.util.AnimalList
import com.yg.mykiwar.util.CommonData
import kotlinx.android.synthetic.main.activity_dict.*
import java.util.*

class DictActivity : AppCompatActivity() {
    private lateinit var arFragment : ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dict)
        arFragment = scene_dict_display as ArFragment
        for (i in 0..10) {
            val name = AnimalList.animalList[Random().nextInt(21)]
            val nameUrl = Uri.parse(AnimalList.getMatch()[name]+".sfb")
            placeObject(arFragment,
                    CommonData.anchor, nameUrl, name)
        }
    }

    fun placeObject(arFragment : ArFragment, anchor: Anchor, model: Uri, name: String) {
        ModelRenderable.builder().setSource(arFragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(arFragment, anchor, renderable, name) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    null
                }
    }

    fun addNodeToScene(arFragment : ArFragment, anchor: Anchor, renderable: Renderable, name: String) {
        val anchorNode = AnchorNode(anchor)
        val node = Node()
        node.renderable = renderable
        node.setParent(anchorNode)
        //Vector3(0f, 0f, 0f)
        val x = Random().nextInt(15) / 10f - Random().nextInt(1) / 10f
        val y = Random().nextInt(15) / 10f
        val z = -(Random().nextInt(15) / 10f)
        node.localPosition = Vector3(x, y, z)
        arFragment.arSceneView.scene.addChild(anchorNode)
    }


//
//    fun getMemberList(id : String) : RealmResults<AnchorInfo> {
//        return realm.where(AnchorInfo::class.java).equalTo("id", id).findAll()
//    }
}
