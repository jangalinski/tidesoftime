package com.github.jangalinski.tidesoftime

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor


sealed class DeckMessage {
  data class DealCard(val deferred: CompletableDeferred<Card>) : DeckMessage()
  data class GetSize(val deferred: CompletableDeferred<Int>) : DeckMessage()
  data class RemainingCards(val deferred: CompletableDeferred<List<Card>>) : DeckMessage()
}

typealias DeckRef = SendChannel<DeckMessage>
suspend fun DeckRef.deal(): Card = CompletableDeferred<Card>().also { this.send(DeckMessage.DealCard(it)) }.await()
suspend fun DeckRef.size(): Int = CompletableDeferred<Int>().also { this.send(DeckMessage.GetSize(it)) }.await()
suspend fun DeckRef.remainingCards(): List<Card> = CompletableDeferred<List<Card>>().also { this.send(DeckMessage.RemainingCards(it)) }.await()

data class ImmutableDeck(val cards: List<Card>) {
  val size = cards.size

  fun deal(): Pair<Card, ImmutableDeck> = cards.first() to copy(cards = cards.drop(1))

  fun remaining() = cards
}

infix fun ImmutableDeck.readOnly(command: (ImmutableDeck) -> Unit): ImmutableDeck {
  command(this)
  return this
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun createDeck(cards: List<Card> = Card.shuffled(), shuffle: Boolean = false): DeckRef = GlobalScope.actor {
  var deck = ImmutableDeck(if (shuffle) cards.shuffled() else cards)

  for (msg in channel) {
    deck = when (msg) {
      is DeckMessage.DealCard -> {
        val (card, newDeck) = deck.deal()
        msg.deferred.complete(card)
        newDeck
      }

      is DeckMessage.GetSize -> deck readOnly { msg.deferred.complete(it.size) }

      is DeckMessage.RemainingCards -> deck readOnly { msg.deferred.complete(deck.remaining()) }
    }
  }
}
