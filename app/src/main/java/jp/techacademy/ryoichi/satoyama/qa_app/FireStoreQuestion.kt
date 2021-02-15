package jp.techacademy.ryoichi.satoyama.qa_app

import java.util.*

class FireStoreQuestion {
    var id = UUID.randomUUID().toString()
    var title = ""
    var body = ""
    var name = ""
    var uid = ""
    var iamge =""
    var genre = 0
    var answers: ArrayList<Answer> = arrayListOf()
}