package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mGenre = 0;

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val map = snapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes = if(imageString.isNotEmpty()) {
                Base64.decode(imageString, Base64.DEFAULT)
            } else {
                byteArrayOf()
            }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if(answerMap != null) {
                for(key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, snapshot.key ?: "", mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val map = snapshot.value as Map<String, String>

            for (question in mQuestionArrayList) {
                if(snapshot.key.equals(question.questionId)) {
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>

                    if(answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["answer"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(findViewById(R.id.toolbar))
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

            if(mGenre == 0) {
                Snackbar.make(view, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
            } else {

            }
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }


        //ナビゲーション
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener (this)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if(id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if(id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if(id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        if(mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        return true
    }

    override fun onResume() {
        super.onResume()

        if(mGenre == 0) {
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }
    }
}