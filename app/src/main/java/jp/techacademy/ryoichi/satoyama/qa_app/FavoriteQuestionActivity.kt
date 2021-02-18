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
    private var mQuestionArrayList = ArrayList<Question>()
    private var questionRef: DatabaseReference? = null
    private var favoriteRef: DatabaseReference? = null

    private val mFavoriteEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            //取得したデータ群をイテレートする
            //お気に入り登録されている質問のIDとジャンルを取得する
            val map = snapshot.value as Map<String, String>
            val questionId = snapshot.key ?: ""
            val genre = map["genre"] ?: ""

            //お気に入り登録されている質問を取得する
            questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(genre).child(questionId)
            questionRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
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
                    val question = Question(title, body, name, uid, snapshot.key ?: "", genre.toInt(), bytes, answerArrayList)

                    mQuestionArrayList.add(question)
                    mAdapter.notifyDataSetChanged()
                }
            })

            //お気に入り質問情報をすべて取得した後に、リストをクリアする
            mQuestionArrayList.clear()
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d("QALOG", "Favorite deleted")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_question)

        //お気に入りリストの準備
        mAdapter = QuestionsListAdapter(this)
        favoriteListView.adapter = mAdapter

        favoriteListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        //お気に入りリストの更新処理
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mAdapter.notifyDataSetChanged()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            favoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(user.uid)
            favoriteRef!!.addChildEventListener(mFavoriteEventListener)
        }
    }
}