package kr.co.petdoc.petdoc.model

sealed class ShoppingSection {
    object BannerSection : ShoppingSection()
    object CustoMealSection : ShoppingSection()
    object ProductSection : ShoppingSection()
}