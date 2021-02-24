package kr.co.petdoc.petdoc.bindingadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_allerge_detail_item.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_0.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_1.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_2.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_3.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_4.view.*
import kotlinx.android.synthetic.main.layout_allerge_danger_5.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.petcheckresult.PetCheckAllergyResultAdapter
import kr.co.petdoc.petdoc.adapter.petcheckresult.PetCheckBloodResultAdapter
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.itemdecoration.VerticalMarginItemDecoration
import kr.co.petdoc.petdoc.model.healthcareresult.AllergyResultDetailItem
import kr.co.petdoc.petdoc.model.healthcareresult.BloodWarning
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection
import kr.co.petdoc.petdoc.viewmodel.MyHealthCareResultViewModel
import kr.co.petdoc.petdoc.widget.BloodResultWarningView

@BindingAdapter("allergyResult")
fun bindAllergyResult(
    recyclerView: RecyclerView,
    items: List<PetCheckResultSection>?
) {
    items?.let {
        recyclerView.adapter = PetCheckAllergyResultAdapter().apply {
            this.items = items
        }
        recyclerView.addItemDecoration(VerticalMarginItemDecoration(
            marginBottom = recyclerView.context.resources.getDimensionPixelOffset(R.dimen.margin_70dp)
        ))
    }
}

@BindingAdapter(value = ["viewModel", "bloodResult"])
fun bindBloodResult(
    recyclerView: RecyclerView,
    viewModel: MyHealthCareResultViewModel,
    items: List<PetCheckResultSection>?
) {
    items?.let {
        recyclerView.adapter = PetCheckBloodResultAdapter(viewModel).apply {
            this.items = items
        }
    }
}

@BindingAdapter("allergyClassItems")
fun bindAllergyClassItems(
    textView: TextView,
    items: List<String>
) {
    textView.text = items.joinToString(separator = ", ") {
        it.replace(",", "/")
    }
}

@BindingAdapter("allergyDanger")
fun bindAllergyClassItems(
    viewGroup: ViewGroup,
    item: AllergyResultDetailItem
) {
    viewGroup.dangerContainer.removeAllViews()
    val layoutInflater = viewGroup.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val step = item.level.toInt()
    val percent = item.percent
    when (step) {
        0 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_0, null)
            viewGroup.addView(view)
            view.with0.post {
                val width = view.with0.width
                view.layoutDanger0.post {
                    val margin = (view.layoutDanger0.width - width) * (percent / 100.0)
                    val param = (view.statusBar0.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar0.layoutParams = param
                }
            }
        }

        1 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_1, null)
            viewGroup.addView(view)
            view.width1.post {
                val width = view.width1.width
                view.layoutDanger1.post {
                    val margin = (view.layoutDanger1.width - width) * (percent / 100.0)
                    val param = (view.statusBar1.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar1.layoutParams = param
                }
            }
        }

        2 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_2, null)
            viewGroup.addView(view)
            view.width2.post {
                val width = view.width2.width
                view.layoutDanger2.post {
                    val margin = (view.layoutDanger2.width - width) * (percent / 100.0)
                    val param = (view.statusBar2.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar2.layoutParams = param
                }
            }
        }

        3 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_3, null)
            viewGroup.addView(view)
            view.width3.post {
                val width = view.width3.width
                view.layoutDanger3.post {
                    val margin = (view.layoutDanger3.width - width) * (percent / 100.0)
                    val param = (view.statusBar3.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar3.layoutParams = param
                }
            }
        }

        4 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_4, null)
            viewGroup.addView(view)
            view.width4.post {
                val width = view.width4.width
                view.layoutDanger4.post {
                    val margin = (view.layoutDanger4.width - width) * (percent / 100.0)
                    val param = (view.statusBar4.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar4.layoutParams = param
                }
            }
        }

        5 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_5, null)
            viewGroup.addView(view)
            view.width5.post {
                val width = view.width5.width
                view.layoutDagner5.post {
                    val margin = (view.layoutDagner5.width - width) * (percent / 100.0)
                    val param = (view.statusBar5.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = margin.toInt()
                    }

                    view.statusBar5.layoutParams = param
                }
            }
        }

        6 -> {
            val view = layoutInflater.inflate(R.layout.layout_allerge_danger_6, null)
            viewGroup.addView(view)
        }
    }
}

@BindingAdapter("bloodWarning")
fun bindBloodWarningView(
    view: BloodResultWarningView,
    item: BloodWarning?
) {
    item?.let {
        view.setWarnings(item)
    }
}

@BindingAdapter("quickSearchCategory")
fun setQuickSearchValue(view: BloodResultWarningView, category: String?) {}

@InverseBindingAdapter(attribute = "quickSearchCategory", event = "onQuickSearchClicked")
fun onQuickSearchClicked(view: BloodResultWarningView): String {
    return view.lastClickedCategory?:""
}

@BindingAdapter("onQuickSearchClicked")
fun setValueChangedListener(view: BloodResultWarningView, valueChanged: InverseBindingListener) {
    view.setBloodResultWarningViewListener(object: BloodResultWarningView.OnQuickSearchButtonListener {
        override fun onQuickSearchButtonClicked() {
            valueChanged.onChange()
        }
    })
}