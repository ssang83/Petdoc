package kr.co.petdoc.petdoc.widget

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kr.co.petdoc.petdoc.R


/**
 * Petdoc
 * Class: NumberPickerDialog
 * Created by kimjoonsung on 2020/04/08.
 *
 * Description : 연령 및 개월 수를 입력 받는 picker dialog
 */
class NumberPickerDialog : DialogFragment() {

    private var mListener: OnValueChangeListener? = null

    private var mTitle:String = ""
    private val minValue = 0
    private var maxValue = 0
    private val step = 1
    private var defaultValue = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mTitle = arguments?.getString("title") ?:""
        maxValue = if (mTitle == "년수") {
            30
        } else {
            11
        }

        val numberPicker = NumberPicker(requireActivity())
        numberPicker.minValue = minValue
        numberPicker.maxValue = maxValue
        numberPicker.displayedValues = getArrayWithSteps(minValue, maxValue, step)
        numberPicker.value = defaultValue
        numberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(mTitle)
        builder.setPositiveButton(getString(R.string.confirm),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    mListener?.onValueChange(
                        numberPicker,
                        numberPicker.value,
                        numberPicker.value
                    )
                }
            })
        builder.setNegativeButton(getString(R.string.cancel),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })

        builder.setView(numberPicker)

        return builder.create()
    }

    //Listener의 setter
    fun setValueChangeListener(valueChangeListener: OnValueChangeListener) {
        mListener = valueChangeListener
    }


    // 최소값부터 최대값까지 일정 간격의 값을 String 배열로 출력
    fun getArrayWithSteps(min: Int, max: Int, step: Int): Array<String?>? {
        val number_of_array = (max - min) / step + 1
        val result = arrayOfNulls<String>(number_of_array)
        for (i in 0 until number_of_array) {
            result[i] = (min + step * i).toString()
        }
        return result
    }
}