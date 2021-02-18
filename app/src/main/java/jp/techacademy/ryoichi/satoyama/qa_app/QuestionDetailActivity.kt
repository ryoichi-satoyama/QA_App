package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private var isFavorite = false;
    private lateinit var mFavorite: Favorite
    private lateinit var mFavoriteRef: DatabaseReference
    private lateinit var questionId: String
    private var favoriteId: String? = null
    private var mQuestionIdList = arrayListOf<String>()

    private val mEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val map = snapshot.value as Map<String, String>

            val answerUid = snapshot.key ?: ""

            for(answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if(answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }
    }

    private val mFavoriteEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            isFavorite = snapshot.value != null
            invalidateOptionsMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener{
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if(user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                //Questionを渡して解答作成画面を起動
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        //回答取得
        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionId).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


        //お気に入り取得
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            mFavoriteRef = databaseReference.child(FavoritesPATH).child(user.uid).child(mQuestion.questionId)
            mFavoriteRef.addValueEventListener(mFavoriteEventListener)
        }

    }

    override fun onResume() {
        super.onResume()
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            menuInflater.inflate(R.menu.menu_question_detail, menu)
//            if(isFavorite) {
//                menu?.findItem(R.id.favoriteButton)?.setIcon(R.drawable.ic_star)
//            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.favoriteButton)?.let {
            it.setIcon(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.favoriteButton) {
            //お気に入り処理
            val user = FirebaseAuth.getInstance().currentUser
            if(user != null) {
                val dataReference = FirebaseDatabase.getInstance().reference
                if(isFavorite) {
                    //お気に入り削除
                    dataReference.child(FavoritesPATH).child(user.uid).child(mQuestion.questionId).removeValue()
                } else {
                    //登録するデータの作成
                    val data = HashMap<String, String>()
                    data["genre"] = mQuestion.genre.toString()

                    //お気に入り登録場所
                    val favoriteRef = dataReference.child(FavoritesPATH).child(user.uid).child(mQuestion.questionId)

                    //お気に入り登録
                    //pushメソッドを使用するとユニークなIDを挟んでdataを登録する
//                    favoriteRef.push().setValue(data)
                    favoriteRef.setValue(data)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }
}