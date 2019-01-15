package com.github.jangalinski.tidesoftime

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce


typealias DeckRef = ReceiveChannel<Card>

@ExperimentalCoroutinesApi
suspend fun createDeck(cards: List<Card> = Card.shuffled(), shuffle: Boolean = false): DeckRef = GlobalScope.produce {
  fun cards(): List<Card> = if (shuffle) cards.shuffled() else cards
  cards().forEach { send(it) }
}

