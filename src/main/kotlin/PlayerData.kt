package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.deck.Card
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.Kingdom

data class Player(
    val name: String,
    val strategy: PlayerStrategy
)

interface PlayerStrategy {
  fun addHandCardToKingdom(data: PlayerData)
  fun chooseKingdomCardToKeep(data: PlayerData)
  fun chooseKingdomCardToDiscard(data: PlayerData)
}

object RandomCardSelection : PlayerStrategy {
  override fun addHandCardToKingdom(data: PlayerData) {
    val card = data.hand.shuffled().first()

    println("random put to kingdom: $card")

    data.toKingdom(data.hand.shuffled().first())
  }

  override fun chooseKingdomCardToKeep(data: PlayerData) {
    val card = data.kingdom.unmarked().shuffled().first().card

    println("random keep from kingdom: $card")

    data.keep(card)

  }

  override fun chooseKingdomCardToDiscard(data: PlayerData) {
    val card = data.kingdom
        .filter { !it.marked }
        .shuffled().first().card

    println("random discard: $card")
    data.discard(card)
  }

  override fun toString(): String = this::class.simpleName!!
}

data class PlayerData(
    var hand: Hand = Hand(),
    var kingdom: Kingdom = Kingdom(),
    val discarded: LinkedHashSet<Card> = LinkedHashSet()
) {
  fun toKingdom(card: Card) {
    hand = hand.remove(card)
    kingdom = kingdom.add(card)
  }

  fun fromKingdom(card : Card) {
    hand = hand.add(card)
    kingdom = kingdom.remove(card)
  }

  fun keep(card: Card) {
    kingdom = kingdom.mark(card)
  }

  fun discard(card : Card) {
    discarded.add(card)
    kingdom =  kingdom.remove(card)
  }

  fun deal(card: Card)  {
    hand = hand.add(card)
  }
}

