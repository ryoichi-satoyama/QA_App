package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.Context
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        mAuth = FirebaseAuth.getInstance()

        mCreateAccountListener = OnCompleteListener { task ->
            if(task.isSuccessful) {
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.create_account_failure_message), Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }
        }

        mLoginListener = OnCompleteListener { task ->
            if(task.isSuccessful) {
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

                if(mIsCreateAccount) {
                    val name = nameText.text.toString()
                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    saveName(name)
                } else {
                    userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            //saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }

                progressBar.visibility = View.GONE
                finish()
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.login_failure_message), Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }
        }

        title = getString(R.string.login_title)

        createButton.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if(email.length != 0 && password.length >= 6 && name.length != 0) {
                mIsCreateAccount = true

                createAccount(email, password)
            } else {
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG).show()
            }
        }

        loginButton.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if(email.length != 0 && password.length >= 6) {
                mIsCreateAccount = false

                login(email, password)
            } else {
                Snackbar.make(v, getString(R.string.login_error_message), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveName(name: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
}