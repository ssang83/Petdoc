package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: SearchActivity
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onBackPressed() {
        if(navSearchFragment.childFragmentManager?.backStackEntryCount == 0){
            finish()
        }else super.onBackPressed()
    }
}