package kr.co.petdoc.petdoc.bindingadapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("thumbnail")
fun bindThumbnail(
    view: ImageView,
    url: String?
) {
    url?.let {
        Glide.with(view.context)
            .load(url)
            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
            .into(view)
    }
}