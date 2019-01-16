package com.github.jangalinski.tidesoftime

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach


sealed class DeckMessage {
  data class DealCard(val deferred: CompletableDeferred<Card>) : DeckMessage()
  data class GetSize(val deferred: CompletableDeferred<Int>) : DeckMessage()
  data class RemainingCards(val deferred: CompletableDeferred<List<Card>>) : DeckMessage()
}

typealias DeckRef = SendChannel<DeckMessage>

suspend fun DeckRef.deal(): Card {
  val deferred = CompletableDeferred<Card>()
  this.send(DeckMessage.DealCard(deferred))
  return deferred.await()
}

suspend fun DeckRef.size(): Int {
  val deferred = CompletableDeferred<Int>()
  this.send(DeckMessage.GetSize(deferred))
  return deferred.await()
}

suspend fun DeckRef.remainingCards(): List<Card> {
  val deferred = CompletableDeferred<List<Card>>()
  this.send(DeckMessage.RemainingCards(deferred))
  return deferred.await()
}

data class ImmutableDeck(val cards: List<Card>) {
  val size = cards.size

  fun deal(): Pair<Card, ImmutableDeck> = cards.first() to copy(cards = cards.drop(1))

  fun remaining() = cards
}


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun createDeck(cards: List<Card> = Card.shuffled(), shuffle: Boolean = false): DeckRef = GlobalScope.actor {

  var deck = ImmutableDeck(if (shuffle) cards.shuffled() else cards)

  consumeEach {
    deck = when (it) {
      is DeckMessage.DealCard -> {
        val (card, newDeck) = deck.deal()
        it.deferred.complete(card)
        newDeck
      }

      is DeckMessage.GetSize -> {
        it.deferred.complete(deck.size)
        deck
      }

      is DeckMessage.RemainingCards -> {
        it.deferred.complete(deck.remaining())
        deck
      }
    }
  }
}
