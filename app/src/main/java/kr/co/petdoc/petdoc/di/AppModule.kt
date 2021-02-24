package kr.co.petdoc.petdoc.di

import android.content.Context
import com.aramhuvis.lite.base.LiteCamera
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.nicepay.NicePayApiService
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.PetdocRepositoryImpl
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.repository.local.cache.AppCacheImpl
import kr.co.petdoc.petdoc.repository.local.preference.BasePreferences
import kr.co.petdoc.petdoc.repository.local.preference.PREF_NAME
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiFactory
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.scanner.PetdocScanner
import kr.co.petdoc.petdoc.scanner.Scanner
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<PersistentPrefs> {
        BasePreferences(androidContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))
    }

    single<Scanner> {
        val context = androidContext()
        PetdocScanner.getInstance(
            context,
            get(),
            get()
        )
    }

    single {
        PetdocApplication.application.apiClient.getApiService()
    }

    single {
        PetdocApiFactory.createApiService()
    }

    single<AppCache> {
        AppCacheImpl()
    }

    single<PetdocRepository> {
        PetdocRepositoryImpl(get(), get())
    }

    factory {
        NicePayApiService.createNicePayApiService()
    }
}