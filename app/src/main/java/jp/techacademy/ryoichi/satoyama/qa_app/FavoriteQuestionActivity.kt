package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite_question.*
import kotlinx.android.synthetic.main.content_main.*

class FavoriteQuestionActivity : AppCompatActivity() {
    private lateinit var mAdapter: QuestionsListAdapter
//    private lateinit var mQuestionArrayList: ArrayList<Question>
    private var mQuestionArrayList = ArrayList<Question>()
    private var mGenre = -1
    private var questionRef: DatabaseReference? = null
    private var favoriteRef: DatabaseReference? = null

    //ユーザごとのお気に入り質問情報を取得する
    private val mFavoriteEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            //onDataChangeはリファレンス配下のデータを丸ごと取得する
            Log.d("QALOG", "FavoriteListener START")

            //取得したデータ群をイテレートする
            for (ss in snapshot.children) {
                //お気に入り登録されている質問のIDとジャンルを取得する
                val questionId = ss.key.toString()
                val genre = ss.child("genre").value.toString()

                //お気に入り登録されている質問を取得する
                questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(genre).child(questionId)
                questionRef!!.addValueEventListener(mQuestionEventListener)
                Log.d("QALOG", "FavoriteListener" + questionId)
            }

            //お気に入り質問情報をすべて取得した後に、リストをクリアする
            mQuestionArrayList.clear()
            Log.d("QALOG", "FavoriteListener END")
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }

    //質問の実データを取得する
    private val mQuestionEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d("QALOG", "QuestionListener START")
            val map = snapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes = if (imageString.isNotEmpty()) {
                Base64.decode(imageString, Base64.DEFAULT)
            } else {
                byteArrayOf()
            }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }
            val question =
                Question(
                    title,
                    body,
                    name,
                    uid,
                    snapshot.key ?: "",
                    mGenre,
                    bytes,
                    answerArrayList
                )

            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
            Log.d("QALOG", "QuestionListener" + snapshot.key)
            Log.d("QALOG", "QuestionListener END")
        }

        override fun onCancelled(firebaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_question)

        mGenre = intent.getIntExtra("genre", -1)

        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        favoriteListView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            favoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(user.uid)
            favoriteRef!!.addValueEventListener(mFavoriteEventListener)
        }

        favoriteListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }

    }
}