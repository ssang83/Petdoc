package kr.co.petdoc.petdoc.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.co.petdoc.petdoc.dialog.LoadingDialog

abstract class PetdocBaseActivity<T: ViewDataBinding, VM: BaseViewModel> : AppCompatActivity() {
    lateinit var binding: T
    private var loadingDialog: LoadingDialog? = null

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getTargetViewModel(): VM

    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        performDataBinding()
    }

    private fun performDataBinding() {
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        binding.setVariable(getBindingVariable(), getTargetViewModel())
    }

    fun getViewDataBinding(): T = binding

    fun showLoadingDialog() {
        if (isFinishing) {
            return
        }

        loadingDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }?: run {
            loadingDialog = LoadingDialog(this)
            loadingDialog!!.show()
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