package kr.co.petdoc.petdoc.db

import android.content.Context
import android.text.TextUtils
import com.couchbase.lite.*
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: KeywordDBManager
 * Created by kimjoonsung on 2020/06/11.
 *
 * Description :
 */
class KeywordDBManager(context: Context){
    companion object {
        private var sSingleton: KeywordDBManager? = null
        const val KEY = "petdocKeywordDb"

        @Synchronized
        fun getInstance(context: Context): KeywordDBManager {
            if (sSingleton == null) {
                sSingleton = KeywordDBManager(context)
            }
            return sSingleton!!
        }
    }

    private var mDatabase: Database? = null

    init {
        if (mDatabase == null) {
            try {
                val cofig = DatabaseConfiguration(context)
                mDatabase = Database(KEY, cofig)
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

    fun getDoc(key: String?): Document? {
        if (TextUtils.isEmpty(key)) {
            return null
        }
        try {
            return mDatabase!!.getDocument(key)
        } catch (e: java.lang.Exception) {
            Logger.p(e)
        }
        return null
    }

    fun load(key: String?): Document? {
        try {
            return mDatabase!!.getDocument(key)
        } catch (e: Exception) {
            Logger.p(e)
        }
        return null
    }

    fun loadAllExt(): ResultSet? {
        var result: ResultSet? = null
        try {
            result = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(mDatabase)).execute()
        } catch (e: CouchbaseLiteException) {
            Logger.p(e)
        }
        return result
    }

    fun loadAllByOrder(): ResultSet? {
        val query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(mDatabase))
            .orderBy(Ordering.property("reg_date").descending())
        var result: ResultSet? = null
        try {
            result = query.execute()
        } catch (e: CouchbaseLiteException) {
            e.printStackTrace()
        }
        return result
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