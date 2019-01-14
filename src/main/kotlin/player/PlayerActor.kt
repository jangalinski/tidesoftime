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

  class DealCard(val card: Card, val size: CompletableDeferred<Int>) : PlayerMessage()
  data class ChooseCardToPlay(val ownHand: Hand, val ownKingdom: Kingdom, val otherKingdom: Kingdom, val cardToPlay: CompletableDeferred<Card>) : PlayerMessage() {
    companion object {
      suspend fun sendTo(player: PlayerActor, ownHand: Hand, ownKingdom: Kingdom, otherKingdom: Kingdom): CompletableDeferred<Card> = with(CompletableDeferred<Card>()){
        player.send(ChooseCardToPlay(ownHand, ownKingdom, otherKingdom, this))
        return this
      }
    }
  }

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
    is PlayerStateQuery -> msg.deferred.complete(state)

    is DealCard -> msg.apply {
      hand = hand.add(card)
      size.complete(hand.size)
    }

    is ChooseCardToPlay -> {
      val cardToPlay = strategy.playHandCard(msg.ownHand)
      println("$name plays $cardToPlay")
      msg.cardToPlay.complete(cardToPlay)

      //msg.other.sendBlocking(PassHand(hand))
    }


  }
}
