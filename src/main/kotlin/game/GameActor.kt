package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Deck
import com.github.jangalinski.tidesoftime.game.GameMessage.CardToKingdom
import com.github.jangalinski.tidesoftime.game.GameMessage.Deal
import com.github.jangalinski.tidesoftime.player.PlayerActor
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import com.github.jangalinski.tidesoftime.player.PlayerMessage.PrintState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor


typealias GameActor = SendChannel<GameMessage>

sealed class GameMessage {
  object Deal : GameMessage()
  object PrintState : GameMessage()
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()
}

@ObsoleteCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor,
    deck: Deck = Deck()): GameActor = GlobalScope.actor {

  val players by lazy {
    arrayOf(player1, player2)
  }


  for (msg in channel) when (msg) {
    is Deal -> do {
      val handSize = players.map { deal(it, deck) }.map { it.await() }
          .distinct()
          .single()
    } while (Hand.SIZE > handSize)

    is GameMessage.PrintState -> players.forEach { it.send(PrintState) }


    is GameMessage.PlayRound -> {
      do {
        val hands = players.map { letPlayerChooseCard(it) }
            .map { (p,f) -> p to f.await() }

        player1.send(PlayerMessage.PassHand(hands[1].second))
        player2.send(PlayerMessage.PassHand(hands[0].second))


      } while(hands[0].second.isNotEmpty())
    }
  }

}

private suspend fun letPlayerChooseCard(p: PlayerActor) : Pair<PlayerActor, CompletableDeferred<Hand>> = with(CompletableDeferred<Hand>()) {
  p.send(PlayerMessage.ChooseCardToPlay(this))
  return p to this
}

private suspend fun deal(p: PlayerActor, deck: Deck): CompletableDeferred<Int> = with(CompletableDeferred<Int>()) {
  p.send(PlayerMessage.DealCard(deck.draw(), this))
  return this
}
