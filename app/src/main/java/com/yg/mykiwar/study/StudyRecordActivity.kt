package com.yg.mykiwar.study

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.yg.mykiwar.R
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_study_record.*

class StudyRecordActivity : AppCompatActivity() {
    private lateinit var arFragment : StudyScanFragment
    private lateinit var name : String
    private var isModelLoaded = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_record)
        arFragment = sceneform_record_fragment as StudyScanFragment

        name = intent.getStringExtra("name")

        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        SpeechRecognizerManager.getInstance().initializeLibrary(this)
        btn_record_pronoun.setOnClickListener {
            image_record_state.visibility = View.VISIBLE
            image_record_state.setImageResource(R.drawable.tory_listen)
            pronoun()
        }
        btn_record_back.setOnClickListener {
            finish()
        }
        tv_record_pronoun.text = name
    }
    private fun onUpdateFrame(frameTime: FrameTime){
        if (arFragment.arSceneView.arFrame!!.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        val cameraPos = arFragment.arSceneView.scene.camera.worldPosition
        val cameraForward = arFragment.arSceneView.scene.camera.forward
        val position = Vector3.add(cameraPos, cameraForward.scaled(1.0f))
        val pose = Pose.makeTranslation(position.x, position.y, position.z)
        Log.v("메소드 캡쳐", "7")

        val anchorNode = AnchorNode(arFragment.arSceneView.session!!.createAnchor(pose))
        if(!isModelLoaded)
            placeObject(arFragment, anchorNode,
                Uri.parse(AnimalList.getMatch()[name] + ".sfb"))
        isModelLoaded = true
    }


    private fun placeObject(fragment : ArFragment, anchor: AnchorNode, model : Uri){
        ModelRenderable.builder().setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
                .exceptionally {
                    Toast.makeText(this, "no render", Toast.LENGTH_SHORT).show()
                    Log.v("메소드 캡쳐", "5")
                    null
                }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: AnchorNode, renderable: Renderable){
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchor)
        fragment.arSceneView.scene.addChild(anchor)
        node.select()
    }

    private fun pronoun(){
        val builder = SpeechRecognizerClient.Builder().
                setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)  // optional

        val client = builder.build()
        var check = ""

        client.startRecording(true)

        client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
            override fun onFinished() {
                //Log.v("음성", "인식 끝")
                if(check == name){
                    Log.v("제대로 인식된 것", name)
                    image_record_state.setImageResource(R.drawable.tory_good_job)
                    Thread.sleep(2000)
                    runOnUiThread {
                        image_record_state.visibility = View.GONE
                    }
                }else{
                    Log.v("잘 인식 안 된 것", check)
                    image_record_state.setImageResource(R.drawable.tory_re)
                    Thread.sleep(2000)
                    runOnUiThread {
                        image_record_state.visibility = View.GONE
                    }
                }
            }

            override fun onPartialResult(partialResult: String?) {
                Log.v("음성", partialResult)
                //partialResult를 갖고 비교
                //계속 도는 부분2
                check = partialResult!!
            }

            override fun onBeginningOfSpeech() {
                //Log.v("음성", "말 시작")

            }

            override fun onAudioLevel(audioLevel: Float) {
                //Log.v("음성", "오디오 레벨 " +  audioLevel.toString())
                //계속 도는 부분1
            }

            override fun onEndOfSpeech() {
                //Log.v("음성", "말 끝")
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                //Log.v("음성", "에러에러")
                //Log.v("음성", errorMsg)
                runOnUiThread {
                    image_record_state.visibility = View.GONE
                }
            }

            override fun onResults(results: Bundle?) {
            }

            override fun onReady() {
                //Log.v("음성", "대기")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }
}
