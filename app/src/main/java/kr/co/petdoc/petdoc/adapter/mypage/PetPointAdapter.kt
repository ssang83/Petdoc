package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_point_history_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.LogData
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: PetPointAdapter
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 펫 포인트 Adapter
 */
class PetPointAdapter : RecyclerView.Adapter<PetPointAdapter.PetPointHolder> () {

    private var mItems:MutableList<LogData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetPointHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_point_history_item, parent, false))

    override fun onBindViewHolder(holder: PetPointHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            if(position != 0) {
                val prevItem = getItem(position - 1)
                val prevPointDate = prevItem!!.pODTM.split(" ")
                val prevDate = prevPointDate[0].split("-")

                val pointDate = item.pODTM.split(" ")
                val date = pointDate[0].split("-")

                val prevMonth = "${prevDate[0]}.${prevDate[1]}"
                val month = "${date[0]}.${date[1]}"

                holder.itemView.historyMonth.apply {
                    when {
                        prevMonth == month -> {
                            visibility = View.GONE
                        }

                        else -> {
                            visibility = View.VISIBLE
                            holder.itemView.historyMonth.text = "${date[0]}.${date[1]}"
                        }
                    }
                }

                holder.itemView.historyDate.text = "${date[1]}.${date[2]}"
            } else {
                val pointDate = item.pODTM.split(" ")
                val date = pointDate[0].split("-")

                holder.itemView.historyMonth.text = "${date[0]}.${date[1]}"
                holder.itemView.historyDate.text = "${date[1]}.${date[2]}"
            }

            holder.itemView.historyTitle.text = item.sAMEDIDPNM
            holder.itemView.historyDetail.text = item.nOTE
            holder.itemView.historyPoint.apply {
                when {
                    item.pOUSEYN == "Y" -> {
                        text = "-${Helper.ToPriceFormat(item.bALANCEAMT)}P"
                        setTextColor(Helper.readColorRes(R.color.orange))
                    }

                    else -> {
                        text = "+${Helper.ToPriceFormat(item.bALANCEAMT)}P"
                        setTextColor(Helper.readColorRes(R.color.color353d5e))
                    }
                }
            }

            holder.itemView.historyType.apply {
                when {
                    item.pOUSEYN == "Y" -> {
                        text = "사용"
                    }

                    else -> {
                        text = "적립예정"
                    }
                }
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun upateData(items: List<LogData>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun getItem(position: Int): LogData? = if (position in 0 until mItems.size) mItems[position] else null

    inner class PetPointHolder(view: View) : RecyclerView.ViewHolder(view)
}