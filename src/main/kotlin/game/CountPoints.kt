package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.CardFeature

fun countPoints(k1: Kingdom, k2: Kingdom): Pair<Result, Result> {
  //  assert(k1.size == k2.size)

  val r1 = Result(k1)
  val r2 = Result(k2)

  arrayOf(r1,r2).forEach {
    missingSymbols(it)
    pointsPerSymbol(it)
    setOfSymbols(it)
  }

  majorityOfSymbols(r1, r2.k)
  majorityOfSymbols(r2, r1.k)

  majorityOfSingle(r1,r2.k)
  majorityOfSingle(r2,r1.k)

  highestSingleCard(r1,r2)
  highestSingleCard(r2,r1)

  return r1 to r2
}

fun majorityOfSymbols(r:Result, other:Kingdom) {
  r.k.filter { it.card.feature is CardFeature.MajorityOfSymbols }.forEach {
    val symbol = (it.card.feature as CardFeature.MajorityOfSymbols).symbol
    val majority = r.k.compare(r.k.effectiveNumberOf[symbol]!!, other.effectiveNumberOf[symbol]!!)

    if (majority) {
      r.points[it.card] = it.card.feature.points
    }
  }
}
fun majorityOfSingle(r:Result, other:Kingdom) {
  r.k.filter { it.card.feature is CardFeature.MajorityOfSingleSymbols }.forEach {
    val majority = r.k.compare(r.k.numberOfSingleSymbols, other.numberOfSingleSymbols)

    if (majority) {
      r.points[it.card] = it.card.feature.points
    }
  }
}

fun pointsPerSymbol(r:Result) {
  r.k.filter { it.card.feature is CardFeature.PointsPerSymbol }.forEach {
    val num = r.k.effectiveNumberOf[(it.card.feature as CardFeature.PointsPerSymbol).symbol]!! * it.card.feature.points
    r.points.put(it.card, num)
  }
}

fun missingSymbols(r: Result) {
  r.k.filter { it.card.feature is CardFeature.PointsPerMissingSymbol }.forEach {
    val numMissing = r.k.numberOf.filter { it.value == 0 }.count()
    r.points[it.card] = numMissing * it.card.feature.points
  }
}

fun setOfSymbols(r:Result) {
  r.k.filter { it.card.feature is CardFeature.PointsForCompleteSetOf }.forEach {
    val set = (it.card.feature as CardFeature.PointsForCompleteSetOf).symbols
    val points = it.card.feature.points

    val numberOfSets = set.map { r.k.numberOf[it]!! }.min()!!

    r.points[it.card] = numberOfSets * points
  }
}

fun highestSingleCard(r: Result, other:Result) {
  r.k.filter { it.card.feature is CardFeature.HighestSingleCardScore }.forEach {
    if (r.k.compare(r.highestScore, other.highestScore)) {
      r.points[it.card] = it.card.feature.points
    }
  }
}

data class Result(val k: Kingdom, val points: MutableMap<Card, Int> = mutableMapOf())  {

  val sum by lazy {
    points.values.sum()
  }

  val highestScore by lazy {
    points.values.max()!!
  }
}

