package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.CardFeature.*
import com.github.jangalinski.tidesoftime.Symbol.*
import kotlinx.coroutines.channels.produce
import java.util.*

data class Deck(
    private val cards: Queue<Card>
) {
  constructor(shuffle: Boolean = true) : this(Card.ordered(), shuffle)
  constructor(list: List<Card>, shuffle: Boolean = true) : this(
      ArrayDeque(if (shuffle) list.shuffled() else list)
  )

  fun remaining() = cards.size
  fun draw() = cards.remove()!!
}

fun GameScope.deck(cards : List<Card> = Card.shuffled()) = produce {
  for (card in cards) send(card)
}

/**
 * A Deck in TidesOfTime has 18 cards.
 *
 * Each card has
 *
 * * a name
 * * a symbol
 * * a feature
 */
enum class Card(
    val symbol: Symbol,
    val feature: CardFeature
) {

  // PER SYMBOL (5)
  DAS_BAD_DER_GOETTER(Forest, PointsPerSymbol(symbol = Tower)),
  DER_VORHOF(Hand, PointsPerSymbol(symbol = Crown)),
  DIE_ZITADELLE_DER_PROPHETEN(Scroll, PointsPerSymbol(symbol = Forest)),
  DIE_FUSSFESSELN_DES_DSCHINNS(Tower, PointsPerSymbol(symbol = Hand)),
  DER_EWIGE_PALAST(Crown, PointsPerSymbol(symbol = Scroll)),

  // MISSING SYMBOL (1)
  DAS_AUGE_DES_NORDENS(Tower, PointsPerMissingSymbol),

  // MAJORITY (5)
  DER_PASS_DES_ALTEN_MANNES(Tower, MajorityOfSymbols(Scroll)),
  DIE_BLUTTRAENENQUELLE(Hand, MajorityOfSymbols(Forest)),
  DIE_ALTERTUEMLICHE_KLUFT(Crown, MajorityOfSymbols(Tower)),
  DER_GOLDENE_TEMPELTURM(Forest, MajorityOfSymbols(Crown)),
  DIE_GROSSE_BIBLIOTHEK_VON_AHN(Scroll, MajorityOfSymbols(Hand)),

  // MAJORITY SINGLE (1)
  DER_MAULWURFSHUEGEL(Symbol.Empty, MajorityOfSingleSymbols),

  // Sets of Symbols (3)
  DAS_LABYRINTH_DER_VERDAMMTEN(Forest, PointsForCompleteSetOf(13, Symbol.actual)),
  DIE_HIMMELSSAEULEN(Hand, PointsForCompleteSetOf(5, setOf(Scroll, Forest))),
  DER_MANABRUNNEN(Scroll, PointsForCompleteSetOf(9, setOf(Crown, Tower, Hand))),

  // META (2)
  DAS_KOENIGSNEST(Symbol.Crown, WinAllDraws),
  DAS_DACH_DER_WELT(Symbol.Empty, DoubleMaxSymbols), // double max symbol

  // HIGHEST SINGLE CARD (1)
  DER_SAPHIRHAFEN(Symbol.Empty, HighestSingleCardScore),
  ;

  companion object {
    fun ordered() = Card.values().toList()
    fun shuffled() = ordered().shuffled()
  }

  override fun toString(): String {
    return "$name($symbol)"
  }


}

enum class Symbol {
  Tower, Crown, Forest, Hand, Scroll, Empty;

  companion object {
    val actual = setOf(Tower, Crown, Forest, Hand, Scroll)
  }
}

sealed class CardFeature(val points: Int) {

  object WinAllDraws : CardFeature(0)
  object DoubleMaxSymbols : CardFeature(0)

  class PointsPerSymbol(val symbol: Symbol) : CardFeature(3)
  object PointsPerMissingSymbol : CardFeature(3)

  class PointsForCompleteSetOf(points: Int, val symbols: Set<Symbol>) : CardFeature(points)
  class MajorityOfSymbols(val symbol: Symbol) : CardFeature(7)
  object MajorityOfSingleSymbols : CardFeature(8)

  object HighestSingleCardScore : CardFeature(8)

}
