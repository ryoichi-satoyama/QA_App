package jp.techacademy.ryoichi.satoyama.qa_app

import java.io.Serializable

class Question(val title: String, val body: String, val name: String, val uid: String, val questionId: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray
    init {
        imageBytes = bytes.clone()
    }
}