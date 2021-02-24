package kr.co.petdoc.petdoc.fragment.chat.v2.desk

import android.os.AsyncTask
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*

/**
 * Petdoc
 * Class: UrlPreviewAsyncTask
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
internal abstract class UrlPreviewAsyncTask : AsyncTask<String?, Void?, UrlPreviewInfo?>() {
    abstract override fun onPostExecute(info: UrlPreviewInfo?)
    override fun doInBackground(vararg params: String?): UrlPreviewInfo? {
        val result = Hashtable<String, String?>()
        val url = params[0]
        val doc: Document
        try {
            doc = Jsoup.connect(url).followRedirects(true).timeout(TIMEOUT_MILLIS).get()
            val titleTags = doc.select("title")
            if (titleTags != null && titleTags.size > 0) {
                result["title"] = titleTags[0].text()
            }
            val descTags = doc.select("meta[name=description]")
            if (descTags != null && descTags.size > 0) {
                result["description"] = descTags[0].attr("content")
            }
            val ogTags = doc.select("meta[property^=og:]")
            for (i in ogTags.indices) {
                val tag = ogTags[i]
                val text = tag.attr("property")
                if ("og:image" == text || "og:image:url" == text) {
                    if (!result.containsKey("image")) {
                        result["image"] = tag.attr("content")
                    }
                } else if ("og:description" == text) {
                    if (!result.containsKey("description")) {
                        result["description"] = tag.attr("content")
                    }
                } else if ("og:title" == text) {
                    if (!result.containsKey("title")) {
                        result["title"] = tag.attr("content")
                    }
                } else if ("og:site_name" == text) {
                    if (!result.containsKey("site_name")) {
                        result["site_name"] = tag.attr("content")
                    }
                } else if ("og:url" == text) {
                    if (!result.containsKey("url")) {
                        result["url"] = tag.attr("content")
                    }
                }
            }
            val twitterTags = doc.select("meta[property^=twitter:]")
            for (i in twitterTags.indices) {
                val tag = twitterTags[i]
                val text = tag.attr("property")
                if ("twitter:image" == text) {
                    if (!result.containsKey("image")) {
                        result["image"] = tag.attr("content")
                    }
                } else if ("twitter:description" == text) {
                    if (!result.containsKey("description")) {
                        result["description"] = tag.attr("content")
                    }
                } else if ("twitter:title" == text) {
                    if (!result.containsKey("title")) {
                        result["title"] = tag.attr("content")
                    }
                } else if ("twitter:site" == text) {
                    if (!result.containsKey("site_name")) {
                        result["site_name"] = tag.attr("content")
                    }
                } else if ("twitter:url" == text) {
                    if (!result.containsKey("url")) {
                        result["url"] = tag.attr("content")
                    }
                }
            }
            if (!result.containsKey("url")) {
                result["url"] = url
            }
            if (result["url"] != null && result["url"]!!.startsWith("//")) {
                result["url"] = "http:" + result["url"]
            }
            if (!result.containsKey("site_name") && result["title"] != null) {
                result["site_name"] = result["title"]
            }
            if (result["image"] != null && result["image"]!!.startsWith("//")) {
                result["image"] = "http:" + result["image"]
            }
            if (result["url"] != null && result["title"] != null && result["image"] != null) {
                return UrlPreviewInfo(
                        result["url"]!!,
                        result["site_name"]!!,
                        result["title"]!!,
                        result["description"]!!,
                        result["image"]!!
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private const val TIMEOUT_MILLIS = 10 * 1000
    }
}