package kr.co.petdoc.petdoc.fragment.chat.v2.desk

import org.json.JSONException
import org.json.JSONObject
import java.net.URL

/**
 * Petdoc
 * Class: UrlPreviewInfo
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
class UrlPreviewInfo {

    var mUrl = ""
    var mSiteName = ""
    var mTitle = ""
    var mDescription = ""
    var mImageUrl = ""

    constructor(url: String, siteName: String, title: String, description: String, imageUrl: String) {
        mUrl = url
        mSiteName = siteName
        mTitle = title
        mDescription = description
        mImageUrl = imageUrl
    }

    @Throws(JSONException::class)
    constructor(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        val bodyObject = jsonObject.getJSONObject("body")

        mUrl = if (bodyObject.has("url")) bodyObject.getString("url") else ""
        mSiteName = if (bodyObject.has("site_name")) bodyObject.getString("site_name") else ""
        mTitle = if (bodyObject.has("title")) bodyObject.getString("title") else ""
        mDescription = if (bodyObject.has("description")) bodyObject.getString("description") else ""
        mImageUrl = if (bodyObject.has("image")) bodyObject.getString("image") else ""
    }

    @Throws(JSONException::class)
    fun toJsonString(): String {
        val bodyObject = JSONObject()
        bodyObject.put("url", mUrl)
        bodyObject.put("site_name", mSiteName)
        bodyObject.put("title", mTitle)
        bodyObject.put("description", mDescription)
        bodyObject.put("image", mImageUrl)

        val jsonObject = JSONObject()
        jsonObject.put("type", DeskUserRichMessage.EVENT_TYPE_URL_PREVIEW)
        jsonObject.put("body", bodyObject)

        return jsonObject.toString()
    }

    fun getDomainName(): String {
        var domainName = ""
        try {
            var urlString = mUrl
            if (urlString != null && urlString.length > 0) {
                urlString = urlString.toLowerCase()
                if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                    urlString = "http://$urlString"
                }
            }
            val url = URL(urlString)
            domainName = url.host
            domainName = if (domainName.startsWith("www.")) domainName.substring(4) else domainName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return domainName
    }
}