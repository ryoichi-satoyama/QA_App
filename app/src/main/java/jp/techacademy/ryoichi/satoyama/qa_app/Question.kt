package jp.techacademy.ryoichi.satoyama.qa_app

import java.io.Serializable

//質問のデータを保持するクラス
//プロパティはvalで定義し、コンストラクタで値を設定
//Intentでデータを渡せるようにSerializableを実装
//title: Firebaseから取得したタイトル
//body: Firebaseから取得した質問本文
//name: Firebaseから取得した質問者の名前
//uid: Firebaseから取得した質問者のUID
//questionUid: Firebaseから取得した質問のUID
//genre: 質問のジャンル
//imageBytes: Firebaseから取得した画像をbyte型の配列にしたもの
//answers: Firebaseから取得した質問のモデルクラスであるAnswerのArrayList
class Question(val title: String, val body: String, val name: String, val uid: String, val questionId: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray
    init {
        imageBytes = bytes.clone()
    }
}