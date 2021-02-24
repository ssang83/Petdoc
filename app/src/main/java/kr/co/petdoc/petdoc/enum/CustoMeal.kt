package kr.co.petdoc.petdoc.enum

/**
 * Petdoc
 * Class: CustoMeal
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
enum class NutritionType(val type: String) {
    STRONG("밸런스"),
    SKIN("피모"),
    JOINT("관절"),
    DIGESTION("소화"),
    IMMUNITY("면역"),
    STONE_KIDNEY("결석&신장"),
    OBESITY("비만")
}

enum class FoodStatus(val color: String) {
    chicken_meet("#70a5d3"),
    salmon_tuna("#6884ce"),
    beef("#d27c50"),
    lamb("#ff8759"),
    duck_meet("#6bbd8b"),
    meet_fish("#ff7865")
}

enum class SpeciesSize(val size: String) {
    XS("초소형"),
    S("소형"),
    M("중형"),
    L("대형"),
    XL("초대형")
}

enum class NutritionStatus(val color: String) {
    obesity("#c886aa"),
    stone_kidney("#7eb602"),
    immunity("#cd8f65"),
    digestion("#cfb602"),
    joint("#7474c8"),
    skin("#658fbf"),
    strong("#ff7865")
}