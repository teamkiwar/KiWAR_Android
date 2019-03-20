package com.yg.mykiwar.play.catchgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.yg.mykiwar.R
import com.yg.mykiwar.play.catchgame.adapter.PlayCatchAdapter
import com.yg.mykiwar.util.AnimalList
import kotlinx.android.synthetic.main.activity_catch_answer.*
import java.util.*



class CatchAnswerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var playCatchAdapter: PlayCatchAdapter
    private lateinit var answerList: ArrayList<String>
    private lateinit var answer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catch_answer)
        //img_catch_answer_clicked.setImageResource(R.drawable.cat)
        answer = intent.getStringExtra("name")
        setAnswerList(intent.getStringExtra("name"))
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
        rv_catch_answer_list.layoutManager = LinearLayoutManager(this)
        rv_catch_answer_list.adapter = playCatchAdapter
    }

    override fun onClick(v: View?) {
        val idx: Int = rv_catch_answer_list!!.getChildAdapterPosition(v)
        val name: String? = answerList[idx]
        if (name == answer) {
            //anchorNode.removeChild(selectedNode)
            Toast.makeText(this, "정답입니다.", Toast.LENGTH_SHORT).show()
            val returnIntent = Intent()
            //returnIntent.putExtra("result", 1001)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else
            Toast.makeText(this, "다시 생각해보세요.", Toast.LENGTH_SHORT).show()
        //frame_catch_list.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
