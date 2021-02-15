package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_question_detail.view.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question): BaseAdapter() {
    companion object {
        //どのレイアウトを使って表示させるかを判断するためのタイプを表す定数
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }
    private var mLayoutInflater: LayoutInflater? = null
    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        //ビューのタイプによってリストのレイアウトを決定する
        if(getItemViewType(position) == TYPE_QUESTION) {
            if(view == null) {
                view = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = view!!.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = view.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if(bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val imageView = view.imageView
                imageView.setImageBitmap(image)
            }
        } else {
            if(view == null) {
                view = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = view!!.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = view.nameTextView as TextView
            nameTextView.text = name

        }
        return view
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    //引数で渡ってきたポジションがどのタイプかを返す
    override fun getItemViewType(position: Int): Int {
        return if(position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }
}