package com.github.jangalinski.tidesoftime

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce


typealias DeckActor = ReceiveChannel<Card>

fun deckActor(shuffle: Boolean = true, cards: List<Card> = Card.ordered()): DeckActor = GlobalScope.produce {
  fun getCards(): List<Card> = if(shuffle) cards.shuffled() else cards
  getCards().map { send(it) }
}

