package kr.co.petdoc.petdoc.fragment.scanner

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scanner_input_passwd.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog

/**
 * Petdoc
 * Class: ScannerInputPasswdFragment
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
class ScannerInputPasswdFragment : Fragment() {

    private lateinit var dataModel:ConnectScannerDataModel

    private var flagBox = booleanArrayOf(false, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_input_passwd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.care_scanner_close_title))
                setMessage(Helper.readStringRes(R.string.care_scanner_close_desc))
                setConfirmButton(Helper.readStringRes(R.string.care_scanner_close_confirm), View.OnClickListener {
                    dismiss()
                    requireActivity().finish()
                })
                setCancelButton(Helper.readStringRes(R.string.care_scanner_close_cancel), View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        btnNext.setOnClickListener {
            dataModel.password.value = editTextPasswd.text.toString()
            dataModel.ssid.value = "PetdocScan_${editTextSSID.text.toString()}"
            findNavController().navigate(R.id.action_scannerInputPasswdFragment_to_scannerConnectFragment)
        }

        showPassword.apply {
            setOnClickListener {
                when {
                    text.toString() == Helper.readStringRes(R.string.password_visible) -> {
                        passwordVisible(true)
                    }

                    else -> passwordVisible(false)
                }
            }
        }

        //====================================================================================================
        editTextSSID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty()!!) {
                    flagBox[0] = false
                } else {
                    flagBox[0] = true
                }

                checkBtnStatus()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editTextPasswd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty()!!) {
                    flagBox[1] = false
                    showPassword.visibility = View.GONE
                } else {
                    flagBox[1] = true
                    showPassword.visibility = View.VISIBLE
                }

                checkBtnStatus()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        showPassword.paintFlags = showPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onResume() {
        super.onResume()

        editTextSSID.setText("")
        editTextPasswd.setText("")

        editTextSSID.requestFocus()
        showKeyBoardOnView(editTextSSID)

        btnNext.isEnabled = false
    }

    /**
     * Open KeyBoard
     *
     * @param v
     */
    protected fun showKeyBoardOnView(v: View) {
        v.requestFocus()
        Helper.showKeyBoard(requireActivity(), v)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        v.requestFocus()
    }

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    protected fun hideKeyboard(v: View) {
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
    }

    private fun checkBtnStatus() {
        btnNext.apply {
            when {
                flagBox[0] == true && flagBox[1] == true -> {
                    setTextColor(Helper.readColorRes(R.color.orange))
                    setBackgroundResource(R.drawable.main_color_round_rect)

                    isEnabled = true
                }

                else -> {
                    setTextColor(Helper.readColorRes(R.color.light_grey3))
                    setBackgroundResource(R.drawable.grey_round_rect)
                }
            }
        }
    }

    private fun passwordVisible(flag: Boolean) {
        if (flag) {
            editTextPasswd.transformationMethod = HideReturnsTransformationMethod.getInstance()
            showPassword.text = Helper.readStringRes(R.string.password_hidden)
            showPassword.paintFlags = showPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            editTextPasswd.transformationMethod = PasswordTransformationMethod.getInstance()
            showPassword.text = Helper.readStringRes(R.string.password_visible)
            showPassword.paintFlags = showPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }
}