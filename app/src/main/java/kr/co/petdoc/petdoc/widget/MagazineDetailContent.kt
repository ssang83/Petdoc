package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_magazine_detail_content.view.*
import kotlinx.android.synthetic.main.view_magazine_detail_image.view.*
import kotlinx.android.synthetic.main.view_magazine_detail_image_content.view.*
import kotlinx.android.synthetic.main.view_magazine_detail_image_title_content.view.*
import kotlinx.android.synthetic.main.view_magazine_detail_question.view.*
import kotlinx.android.synthetic.main.view_magazine_detail_title_content.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants

/**
 * Petdoc
 * Class: MagazineDetailImageContent
 * Created by kimjoonsung on 2020/06/04.
 *
 * Description :
 */
class MagazineImageContent(context: Context, image: String, content: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_image_content, null, false)

        Glide.with(mContext)
            .load(if(image.startsWith("http")) image else "${AppConstants.IMAGE_URL}${image}")
            .into(view.contentImg1)

        view.content1.text = content

        addView(view)
    }
}

class MagazineQuestion(context: Context, question: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_question, null, false)

        view.question.text = question

        addView(view)
    }
}

class MagazineTitle(context: Context, title: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_content, null, false)

        view.content2.text = title

        addView(view)
    }
}

class MagazineTitleContent(context: Context, title: String, content: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_title_content, null, false)

        view.contentTitle1.text = title
        view.content3.text = content

        addView(view)
    }
}

class MagazineImageTitleContent(context: Context, title: String, content: String, image: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_image_title_content, null, false)

        view.contentTitle2.text = title
        view.content4.text = content

        Glide.with(mContext)
            .load(if(image.startsWith("http")) image else "${AppConstants.IMAGE_URL}${image}")
            .into(view.contentImg2)

        addView(view)
    }
}

class MagazineImage(context: Context, image: String) :
    FrameLayout(context) {

    private val mContext = context

    init {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_magazine_detail_image, null, false)

        Glide.with(mContext)
            .load(if(image.startsWith("http")) image else "${AppConstants.IMAGE_URL}${image}")
            .into(view.contentImg3)

        addView(view)
    }
}