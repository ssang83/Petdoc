package kr.co.petdoc.petdoc.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_level_check.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: LevelCheckDialogFragment
 * Created by kimjoonsung on 2020/08/06.
 *
 * Description :
 */
class LevelCheckDialogFragment(private val callback:CallbackListener) : DialogFragment() {

    private var level = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return inflater.inflate(R.layout.dialog_level_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        level = arguments?.getString("level") ?: level

        btnClose.setOnClickListener { dismiss() }
        btnRecord.setOnClickListener {
            callback.goToRecord()
            dismiss()
        }

        levelStatus.text = getLevelTitle(level)
        description.text = getLevelContent(level)

        setLevelImg(level)
    }

    private fun getLevelTitle(level: String) =
        when (level) {
            "max" -> "축하합니다!/n당신은 만점주이니!"
            "down" -> "레벨이 떨어졌어요!"
            "up" -> "레벨이 올랐어요!"
            "same" -> "이번 달도 같은 레벨!"
            else -> ""
        }

    private fun getLevelContent(level: String) =
        when (level) {
            "max" -> "최고의 주이니! 최고 레벨 혜택도\n꼭 확인하세요♡"
            "down" -> "기록을 깜박한 거죠?\n다시 레벨업을 향해 고고!"
            "up" -> "만점주이니가 눈앞에 있어요!"
            "same" -> "만점주이니를 위해 조금 더\n분발해봐요!"
            else -> ""
        }

    private fun setLevelImg(level: String) {
        when (level) {
            "up" -> levelImg.setBackgroundResource(R.drawable.ic_level_up)
            "down" -> levelImg.setBackgroundResource(R.drawable.ic_level_down)
            "same" -> levelImg.setBackgroundResource(R.drawable.ic_level_same)
        }
    }

    interface CallbackListener {
        fun goToRecord()
    }
}