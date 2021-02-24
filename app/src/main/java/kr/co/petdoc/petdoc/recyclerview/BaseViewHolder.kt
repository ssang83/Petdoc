package kr.co.petdoc.petdoc.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: BaseViewHolder
 * Created by kimjoonsung on 2020/09/28.
 *
 * Description :
 */
abstract class BaseViewHolder<in ITEM : Any>(
    val context: Context,
    @LayoutRes layoutRes: Int,
    parent: ViewGroup?
) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(layoutRes, parent, false)) {

    fun onBindViewHolder(item: Any?) {
        try {
            onViewCreated(item as? ITEM?)
        } catch (e: Exception) {
            Logger.p(e)
            itemView.visibility = View.GONE
        }
    }

    abstract fun onViewCreated(item: ITEM?)
}