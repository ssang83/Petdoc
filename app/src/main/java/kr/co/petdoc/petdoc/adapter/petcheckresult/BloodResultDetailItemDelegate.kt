package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_blood_detail_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemDetailBinding
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultDetailItem
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection
import kr.co.petdoc.petdoc.utils.Helper

class BloodResultDetailItemDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodResultDetailItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return DetailItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_detail,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        items: List<PetCheckResultSection>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    ) {
        val item = items[position] as BloodResultDetailItem
        (holder as DetailItemViewHolder).bind(item)
    }

    inner class DetailItemViewHolder(view: View): BindingViewHolder<BloodResultDelegateItemDetailBinding>(view) {
        var resultRate = 0.0
        var minRate = 0.0
        var minReferRate = 0.0
        var maxRate = 0.0
        var maxReferRate = 0.0

        fun bind(item: BloodResultDetailItem) {
            binding.item = item
            resultRate = item.resultRate.toDouble()
            minRate = item.minRate.toDouble()
            minReferRate = item.referMinRate.toDouble()
            maxRate = item.maxRate.toDouble()
            maxReferRate = item.referMaxRate.toDouble()

            resetViews()

            if (item.referMinRate == "0.0" && item.referMaxRate == "0.0") {
                itemView.layoutStatus.visibility = View.GONE
                itemView.layoutStatus2.visibility = View.GONE
            } else {
                if(item.referMinRate == "0.0") {
                    setStatus2(item.level)
                } else {
                    setStatus(item.level)
                }
            }
        }

        private fun resetViews() {
            itemView.rate.setTextColor(Helper.readColorRes(R.color.slateGrey))
            itemView.normal2.setBackgroundResource(R.drawable.bg_negative)
            itemView.normal2.setTextColor(Helper.readColorRes(R.color.light_grey3))
            itemView.high2.setBackgroundResource(R.drawable.white_right_only_round)
            itemView.high2.setTextColor(Helper.readColorRes(R.color.light_grey3))
            itemView.rate.setTextColor(Helper.readColorRes(R.color.slateGrey))
            itemView.low.setBackgroundResource(R.drawable.white_left_only_round)
            itemView.low.setTextColor(Helper.readColorRes(R.color.light_grey3))
            itemView.normal.setBackgroundResource(R.drawable.bg_value_normal)
            itemView.normal.setTextColor(Helper.readColorRes(R.color.light_grey3))
            itemView.high.setBackgroundResource(R.drawable.white_right_only_round)
            itemView.high.setTextColor(Helper.readColorRes(R.color.light_grey3))
            itemView.rate.setTextColor(Helper.readColorRes(R.color.slateGrey))
        }

        private fun setStatus2(_status: String) {
            itemView.layoutStatus.visibility = View.GONE
            itemView.layoutStatus2.visibility = View.VISIBLE

            var percent = 0.0
            when (_status) {
                "Normal" -> {
                    val range = ((resultRate - minRate) / (maxReferRate - minRate)) * 100.0
                    percent = range / 2.0

                    itemView.normal2.setBackgroundResource(R.drawable.bluegrey_left_round_sold_3)
                    itemView.normal2.setTextColor(Helper.readColorRes(R.color.white))
                    itemView.rate.setTextColor(Helper.readColorRes(R.color.slateGrey))
                    itemView.statusBar2.setBackgroundResource(R.color.slateGrey)
                }
                "High" -> {
                    percent = 50.0
                    val range = ((resultRate - maxReferRate) / (maxRate - maxReferRate)) * 100.0
                    percent += range / 2.0

                    itemView.high2.setBackgroundResource(R.drawable.bg_value_high)
                    itemView.high2.setTextColor(Helper.readColorRes(R.color.white))
                    itemView.rate.setTextColor(Helper.readColorRes(R.color.orange))
                    itemView.statusBar2.setBackgroundResource(R.color.orange)
                }
            }

            itemView.statusBar2.visibility = View.VISIBLE
            itemView.layoutStatus2.post {
                val position = itemView.layoutStatus2.width * (percent / 100.0)
                val param = (itemView.statusBar2.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = position.toInt()
                }
                itemView.statusBar2.layoutParams = param
            }
        }

        private fun setStatus(_status: String) {
            itemView.layoutStatus.visibility = View.VISIBLE
            itemView.layoutStatus2.visibility = View.GONE

            var percent = 0.0
            when (_status) {
                "Low" -> {
                    val range = ((resultRate - minRate) / (minReferRate - minRate)) * 100.0
                    percent = range / 3.0
                    itemView.low.setBackgroundResource(R.drawable.bg_value_low)
                    itemView.low.setTextColor(Helper.readColorRes(R.color.white))
                    itemView.rate.setTextColor(Helper.readColorRes(R.color.cornflower))
                    itemView.statusBar.setBackgroundResource(R.color.cornflower)
                }
                "Normal" -> {
                    percent = 33.0
                    val range = ((resultRate - minReferRate) / (maxReferRate - minReferRate)) * 100.0
                    percent += range / 3.0

                    itemView.normal.setBackgroundResource(R.color.blueyGrey)
                    itemView.normal.setTextColor(Helper.readColorRes(R.color.white))
                    itemView.rate.setTextColor(Helper.readColorRes(R.color.slateGrey))
                    itemView.statusBar.setBackgroundResource(R.color.slateGrey)
                }
                "High" -> {
                    percent = 66.0
                    val range = ((resultRate - maxReferRate) / (maxRate - maxReferRate)) * 100.0
                    percent += range / 3.0

                    itemView.high.setBackgroundResource(R.drawable.bg_value_high)
                    itemView.high.setTextColor(Helper.readColorRes(R.color.white))
                    itemView.rate.setTextColor(Helper.readColorRes(R.color.orange))
                    itemView.statusBar.setBackgroundResource(R.color.orange)
                }
            }

            itemView.layoutStatus.post {
                val position = itemView.layoutStatus.width * (percent / 100.0)
                val param = (itemView.statusBar.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = position.toInt()
                }
                itemView.statusBar.layoutParams = param
            }
        }
    }
}