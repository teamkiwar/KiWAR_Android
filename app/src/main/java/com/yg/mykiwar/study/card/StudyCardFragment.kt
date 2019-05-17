package com.yg.mykiwar.study.card

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.yg.mykiwar.R
import com.yg.mykiwar.study.StudyScanActivity
import kotlinx.android.synthetic.main.fragment_study_card.*

class StudyCardFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_study_card, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SpeechRecognizerManager.getInstance().initializeLibrary(activity)
    }

    override fun onStart() {
        super.onStart()
        val name = arguments!!.getString("imageName")
        tv_study_card.text = name
        img_study_card.setBackgroundResource(arguments!!.getInt("imageUri"))
        btn_study_scan.setOnClickListener {
            val intent = Intent(context, StudyScanActivity::class.java)
            intent.putExtra("name", name)
            activity!!.startActivity(intent)
        }

        val builder = SpeechRecognizerClient.Builder().
                setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)  // optional

        val client = builder.build()
        var check = ""

        btn_study_pronounc.setOnClickListener {
            client.startRecording(true)
        }


        client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
            override fun onFinished() {
                //Log.v("음성", "인식 끝")
                if(check == name){
                    Toast.makeText(context, "참 잘했어요!", Toast.LENGTH_SHORT).show()
                    Log.v("제대로 인식된 것", name)
                }else{
                    Toast.makeText(context, "다시 해보세요", Toast.LENGTH_SHORT).show()
                    Log.v("잘 인식 안 된 것", check)
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
            }

            override fun onResults(results: Bundle?) {
            }

            override fun onReady() {
                //Log.v("음성", "대기")

                //To change body of implemented methods use File | Settings | File Templates.
            }
        })
    }

    companion object {
        fun create(imageName: String, imageUri : Int): StudyCardFragment {
            val fragment = StudyCardFragment()
            val args = Bundle()
            args.putString("imageName", imageName)
            args.putInt("imageUri", imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }
}
