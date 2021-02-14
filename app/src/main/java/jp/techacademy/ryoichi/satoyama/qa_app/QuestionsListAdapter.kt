package jp.techacademy.ryoichi.satoyama.qa_app

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_questions.view.*

class QuestionsListAdapter(context: Context): BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mQuestionArrayList = ArrayList<Question>()
    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.list_questions, parent, false)
        }

        val titleText = view!!.titleTextView as TextView
        titleText.text = mQuestionArrayList[position].title

        val nameText = view.nameTextView as TextView
        nameText.text = mQuestionArrayList[position].name

        val resText = view.resTextView as TextView
        val resNum = mQuestionArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = mQuestionArrayList[position].imageBytes
        if(bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val imageView = view.imageView as ImageView
            imageView.setImageBitmap(image)
        }

        return  view
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        mQuestionArrayList = questionArrayList
    }

    override fun getItem(position: Int): Any {
        return mQuestionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mQuestionArrayList.size
    }
}