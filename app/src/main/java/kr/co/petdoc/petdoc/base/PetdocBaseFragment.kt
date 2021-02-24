package kr.co.petdoc.petdoc.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kr.co.petdoc.petdoc.dialog.LoadingDialog

abstract class PetdocBaseFragment<T: ViewDataBinding, VM: BaseViewModel> : Fragment() {
    protected lateinit var binding: T
    private var loadingDialog: LoadingDialog? = null

    abstract fun getTargetViewModel(): VM

    abstract fun getBindingVariable(): Int

    @LayoutRes
    abstract fun getLayoutId(): Int

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<T>(
                inflater,
                getLayoutId(),
                container,
                false
        ).apply {
            binding = this
            binding.lifecycleOwner = viewLifecycleOwner
            binding.setVariable(getBindingVariable(), getTargetViewModel())
        }.root
    }

    fun showLoadingDialog() {
        if (isDetached) {
            return
        }

        loadingDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }?: run {
            activity?.let {
                loadingDialog = LoadingDialog(it)
                loadingDialog!!.show()
            }
        }
    }

    fun dismissLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}