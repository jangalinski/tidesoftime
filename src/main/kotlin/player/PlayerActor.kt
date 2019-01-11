package com.github.jangalinski.tidesoftime.player

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.Kingdom
import com.github.jangalinski.tidesoftime.player.PlayerMessage.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

typealias PlayerActor = SendChannel<PlayerMessage>

sealed class PlayerMessage {
  data class StartRound(val hand: Hand) : PlayerMessage()

  class DealCard(val card: Card, val size: CompletableDeferred<Int>) : PlayerMessage()
  class ChooseCardToPlay(val hand: CompletableDeferred<Hand>) : PlayerMessage()

  class PassHand(val hand: Hand) : PlayerMessage()

  data class PlayerStateQuery(val deferred: CompletableDeferred<PlayerState>) : PlayerMessage() {
    companion object {
      suspend fun sendTo(player: PlayerActor): CompletableDeferred<PlayerState> = with(CompletableDeferred<PlayerState>()){
        player.send(PlayerStateQuery(this))
        return this
      }
    }
  }

}

data class PlayerState(
  val remainingCardsInDeck: Set<Card> = Card.values().toSet(),
  val discardedCards : Set<Card> = setOf(),
  val myHand: Hand = Hand(),
  val myKingdom: Kingdom = Kingdom(),
  val otherHand: Hand = Hand(),
  val otherKingdom: Kingdom = Kingdom()
)

@ObsoleteCoroutinesApi
fun player(
    name: String,
    strategy: PlayerStrategy): PlayerActor = GlobalScope.actor {

  var state = PlayerState()

  var hand = Hand()
  var kingdom = Kingdom()

  for (msg in channel) when (msg) {

    is StartRound -> state = state.copy(myHand = msg.hand)

    is PlayerStateQuery -> msg.deferred.complete(state)

    is DealCard -> msg.apply {
      hand = hand.add(card)
      size.complete(hand.size)
    }

    is ChooseCardToPlay -> {
      val card = strategy.playHandCard(hand)
      hand = hand.remove(card)
      kingdom = kingdom.add(card)

      println("[${hand.size}] $name plays $card")

      //msg.other.sendBlocking(PassHand(hand))
    }

    is PassHand -> hand = msg.hand
  }
}
