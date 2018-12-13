package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Card

data class Hand(
    private val cards: Set<Card> = EMPTY
) : Collection<Card> by cards {
  companion object {
    val EMPTY = setOf<Card>()
    const val SIZE  = 5
  }
  fun add(card: Card) = copy(cards = cards.toMutableSet().apply { add(card) })
  fun remove(card: Card) = copy(cards = cards.toMutableSet().apply { remove(card) })
}
