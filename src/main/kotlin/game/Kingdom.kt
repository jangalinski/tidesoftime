package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.deck.Card
import com.github.jangalinski.tidesoftime.deck.CardFeature
import com.github.jangalinski.tidesoftime.deck.Symbol


data class Kingdom(
    private val cards: Set<KingdomCard> = setOf()
) : Collection<KingdomCard> by cards {
  fun add(card: Card) = add(KingdomCard(card))
  fun add(card: KingdomCard) = copy(cards = LinkedHashSet(cards).apply { add(card) })

  fun marked() = cards.filter { it.marked }
  fun unmarked() = cards.filter { !it.marked }

  fun remove(card: Card) = copy(cards = LinkedHashSet(cards).apply { removeIf { it.card == card } })

  fun mark(card: Card) = remove(card).add(KingdomCard(card, true))

  val winsAllDraws: Boolean = cards.any { it.card.feature == CardFeature.WinAllDraws }
  val doublesMaxSymbols: Boolean = cards.any { it.card.feature == CardFeature.DoubleMaxSymbols }

  val numberOf: Map<Symbol, Int> by lazy {
    Symbol.actual.map { s ->
      s to cards.count { it.card.symbol == s }
    }.toMap()}

  val effectiveNumberOf by lazy {
    when (doublesMaxSymbols) {
      true -> {
        val max = numberOf.values.max()!!
        numberOf.map { (k, v) -> k to if (max == v) v * 2 else v }.toMap()
      }
      else -> numberOf
    }
  }

  val numberOfSingleSymbols by lazy {
    numberOf.values.filter { it == 1 }.count()
  }

  fun compare(my:Int, other:Int) : Boolean = (my > other) || (my == other && winsAllDraws)
}

data class KingdomCard(val card: Card, val marked: Boolean = false)
