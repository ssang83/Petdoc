package kr.co.petdoc.petdoc.extensions

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.io() = subscribeOn(Schedulers.io())

fun Completable.io() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())