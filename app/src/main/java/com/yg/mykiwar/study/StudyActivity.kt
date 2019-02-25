package com.yg.mykiwar.study

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.yg.mykiwar.R

import kotlinx.android.synthetic.main.activity_study.*

class StudyActivity : AppCompatActivity() {

    var text : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        SpeechRecognizerManager.getInstance().initializeLibrary(this)

        val builder = SpeechRecognizerClient.Builder().
                setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)  // optional

        val client = builder.build()

        btn_study_pronoun.setOnClickListener {
            client.startRecording(true)
        }

        client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
            override fun onFinished() {
                //Log.v("음성", "인식 끝")
            }

            override fun onPartialResult(partialResult: String?) {
                Log.v("음성", partialResult)
                //partialResult를 갖고 비교
                //계속 도는 부분2
                text = partialResult
                tv_study_result.text = text
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
            // ...
        })

    }

    public override fun onDestroy() {
        super.onDestroy()

        SpeechRecognizerManager.getInstance().finalizeLibrary()
    }

}
