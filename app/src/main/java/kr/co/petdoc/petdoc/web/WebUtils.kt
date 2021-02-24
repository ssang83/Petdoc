package kr.co.petdoc.petdoc.web

import android.util.Patterns
import java.util.*

/**
 * Petdoc
 * Class: WebUtils
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
object WebUtils {

    fun extractUrls(input: String): List<String> {
        val result: MutableList<String> = ArrayList()
        val pattern = Patterns.WEB_URL
        val words = input.split("\\s+".toRegex()).toTypedArray()
        for (word in words) {
            var url = word
            if (pattern.matcher(word).find()) {
                if (word.toLowerCase().contains("http://").not() && word.toLowerCase().contains("https://").not()) {
                    url = "http://$word"
                }
                result.add(url)
            }
        }

        return result
    }
}