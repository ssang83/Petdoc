package kr.co.petdoc.petdoc.adapter.care

/**
 * Petdoc
 * Class: CareDate
 * Created by kimjoonsung on 2020/06/08.
 *
 * Description :
 */
data class Day(var year : String, var month : String, var day : String)

data class Week(var day1 : Day, var day2 : Day, var day3 : Day, var day4 : Day, var day5 : Day, var day6 : Day, var day7 : Day)