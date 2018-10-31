package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.deck.Card

data class Hand(
    private val cards: Set<Card> = EMPTY
) : Collection<Card> by cards {
  companion object {
    val EMPTY = setOf<Card>()
  }
  fun add(card: Card) = copy(cards = LinkedHashSet(cards).apply { add(card) })
  fun remove(card: Card) = copy(cards = LinkedHashSet(cards).apply { remove(card) })
}
