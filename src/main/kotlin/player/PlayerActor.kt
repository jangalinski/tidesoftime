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
  object PrintState : PlayerMessage()
  class ChooseCardToPlay(val hand: CompletableDeferred<Hand>) : PlayerMessage()

  class PassHand(val hand: Hand) : PlayerMessage()
}


@ObsoleteCoroutinesApi
fun player(
    name: String,
    strategy: PlayerStrategy): PlayerActor = GlobalScope.actor {

  var hand = Hand()
  var kingdom = Kingdom()

  for (msg in channel) when (msg) {

    is PrintState -> println("""Player: $name
            Hand: $hand
            Kingdom: $kingdom
          """.trimMargin())

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
