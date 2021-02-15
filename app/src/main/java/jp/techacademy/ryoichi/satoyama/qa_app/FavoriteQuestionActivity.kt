package jp.techacademy.ryoichi.satoyama.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite_question.*

class FavoriteQuestionActivity : AppCompatActivity() {
    private lateinit var mAdapter: QuestionsListAdapter
    private lateinit var mDatabaseReference: FirebaseDatabase
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mFavoriteQuestionList: ArrayList<HashMap<String?, String>>
    private var mGenre = -1

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            //リスナーを設定したタイミングで、リファレンスにデータがあれば、onChildAddedのメソッドがデータ数分呼ばれる
            //データが0件場合は、onChildAddedは呼ばれない
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
                Question(title, body, name, uid, snapshot.key ?: "", mGenre, bytes, answerArrayList)

            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
            //失敗したら
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            //データを移動したとき
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            //データを削除したとき
        }
    }

    private val mFavoriteEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val map = snapshot.value as Map<String, String>
            val questionId = snapshot.key
            val genre = map["genre"] ?: ""
//            val questionMap = HashMap<String?, String>()
//            questionMap[questionId] = genre
//            mFavoriteQuestionList.add(questionMap)

            val questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(genre).equalTo(questionId)
            questionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
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
                        Question(title, body, name, uid, snapshot.key ?: "", mGenre, bytes, answerArrayList)

                    mQuestionArrayList.add(question)
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            })
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
            //失敗したら
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            //データを移動したとき
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            //データを削除したとき
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_question)

        mGenre = intent.getIntExtra("genre", -1)

        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        favoriteListView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            val favoriteRef = FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(user.uid)
            favoriteRef.addChildEventListener(mFavoriteEventListener)
        }

//        val questionRef = FirebaseDatabase.getInstance().reference.child(ContentsPATH).child(mGenre.toString())
//        questionRef.addChildEventListener(mEventListener)
    }
}