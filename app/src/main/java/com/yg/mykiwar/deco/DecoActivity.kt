package com.yg.mykiwar.deco

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yg.mykiwar.R
import kotlinx.android.synthetic.main.activity_deco.*

class DecoActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deco)


//        setNewCloudAnchor(null)
//
//        fragment = fragment_place as PlaceFragment
//        fragment.planeDiscoveryController.hide()
//        fragment.arSceneView.scene.addOnUpdateListener(this::onUpadteFrame)
//
//        fragment.setOnTapArPlaneListener { hitResult, plane, _ ->
//            if((plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) or (placeAnchorState != PlaceAnchorState.NONE))
//                return@setOnTapArPlaneListener
//            val newAnchor = fragment.arSceneView.session!!.hostCloudAnchor(hitResult.createAnchor())
//            setNewCloudAnchor(newAnchor)
//            placeAnchorState = PlaceAnchorState.HOSTING
//            snackbarHelper.showMessage(this, "Now hosting anchor...")
//
//            placeObject(fragment, cloudAnchor, Uri.parse("penguin1.sfb"))
//

        btn_1.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 1)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_2.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 2)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_3.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 3)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_4.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 4)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_5.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 5)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_6.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 6)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_7.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 7)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_8.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 8)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_9.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 9)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_10.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 10)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_11.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 11)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_12.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 12)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_13.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 13)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_14.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 14)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_15.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 15)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_16.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 16)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_17.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 17)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_18.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 18)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

        btn_19.setOnClickListener {
            val intent = Intent(this, SizeCheckActivity::class.java)
            intent.putExtra("name", 19)
            startActivity(intent)
//            startActivity(Intent(this, SizeCheckActivity::class.java))
        }

    }

}
