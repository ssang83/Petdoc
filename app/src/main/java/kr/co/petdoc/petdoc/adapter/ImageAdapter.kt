package kr.co.petdoc.petdoc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_image.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.ImageResult

/**
 * Petdoc
 * Class: ImageAdapter
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description :
 */
class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    private var mItems:MutableList<ImageResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ImageHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_image, parent, false))

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.txtID.text = item.id.toString()
            holder.itemView.content.text = item.name

            Glide.with(holder.itemView.context)
                .load(item.img)
                .apply(RequestOptions.centerCropTransform().placeholder(R.mipmap.ic_launcher))
                .into(holder.itemView.imageView)
        }

        holder.itemView.setOnClickListener { Logger.d("position ${position}") }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items:MutableList<ImageResult>) {
        mItems = items
        notifyDataSetChanged()
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}