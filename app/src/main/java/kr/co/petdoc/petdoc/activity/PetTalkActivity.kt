package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_pettalk.*
import kr.co.petdoc.petdoc.R

class PetTalkActivity : AppCompatActivity() {

    companion object {
        lateinit var instance:PetTalkActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        val viewModel = ViewModelProvider(this).get(PetTalkLiveModel::class.java)

        intent.extras?.apply{
            intent.getStringExtra("category")?.let{it->
                viewModel.choice_category.value = it
            }
        }

        setContentView(R.layout.activity_pettalk)
    }

    override fun onBackPressed() {
        if(nav_pettalk_fragment.childFragmentManager?.backStackEntryCount == 0){
            finish()
        }else super.onBackPressed()
    }

    fun popBackStack() {
        findNavController(R.id.nav_pettalk_fragment).popBackStack()
    }
}


class PetTalkLiveModel : ViewModel(){

    var choice_category : MutableLiveData<String> = MutableLiveData<String>().apply{ value = "" }
    var jump_mode : MutableLiveData<String> = MutableLiveData<String>().apply{ value = "" }
}