package kr.co.petdoc.petdoc.db

import android.content.Context
import android.text.TextUtils
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.Document
import com.couchbase.lite.MutableDocument
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: DBManager
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description :
 */
class DBManager(context: Context) {

    companion object {
        private var sSingleton: DBManager? = null

        @Synchronized
        fun getInstance(context: Context): DBManager {
            if (sSingleton == null) {
                sSingleton = DBManager(context)
            }
            return sSingleton!!
        }
    }

    private var mDatabase: Database? = null

    init {
        if (mDatabase == null) {
            try {
                val cofig = DatabaseConfiguration(context)
                mDatabase = Database("petdocDb", cofig)
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    fun put(document: MutableDocument?): Boolean {
        try {
            mDatabase!!.save(document)
        } catch (e: Exception) {
            Logger.p(e)
            return false
        } finally {
        }
        return true
    }

    operator fun get(key: String?): String? {
        if (TextUtils.isEmpty(key)) {
            return ""
        }
        try {
            val document = mDatabase!!.getDocument(key)
            return if (document != null) {
                document.getString(key)
            } else {
                ""
            }
            //return URLEncoder.encode(data, "UTF-8");
        } catch (e: Exception) {
            Logger.p(e)
        }
        return ""
    }

    fun load(key: String?): Document? {
        try {
            return mDatabase!!.getDocument(key)
        } catch (e: Exception) {
            Logger.p(e)
        }
        return null
    }

    fun delete(key: String?): Boolean {
        try {
            mDatabase!!.deleteIndex(key)
        } catch (e: Exception) {
            Logger.p(e)
            return false
        }
        return true
    }

    fun deleteDocument(key: String?): Boolean {
        try {
            val document = mDatabase!!.getDocument(key)
            if (document != null) {
                mDatabase!!.delete(document)
            }
        } catch (e: Exception) {
            Logger.p(e)
            return false
        }
        return true
    }

    fun delete(document: Document?): Boolean {
        try {
            mDatabase!!.delete(document)
        } catch (e: Exception) {
            Logger.p(e)
            return false
        }
        return true
    }

    @Synchronized
    fun deleteAll() {
        try {
            mDatabase!!.delete()
            sSingleton = null
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

}