package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Deck
import com.github.jangalinski.tidesoftime.game.GameMessage.CardToKingdom
import com.github.jangalinski.tidesoftime.player.PlayerActor
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import com.github.jangalinski.tidesoftime.player.PlayerState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor


typealias GameActor = SendChannel<GameMessage>

sealed class GameMessage {
  object PrintState : GameMessage()
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()

  data class GameStateQuery(val deferred: CompletableDeferred<GameState>) : GameMessage() {
    companion object {
      suspend fun sendTo(game: GameActor): CompletableDeferred<GameState> = with(CompletableDeferred<GameState>()){
        game.send(GameStateQuery(this))
        return this
      }
    }
  }
}

data class GameState(
    val deck: Deck,
    val handPlayer1: Hand = Hand(), val kingdomPlayer1: Kingdom = Kingdom(),
    val handPlayer2: Hand = Hand(), val kingdomPlayer2: Kingdom = Kingdom()
) {

  val handSize : Int by lazy {
    arrayOf(handPlayer1, handPlayer2).map { it.size }.distinct().single()
  }

  fun deal(): GameState {
    if(handSize == Hand.SIZE) {
      return this
    }
    return copy(handPlayer1 = handPlayer1.add(deck.draw()), handPlayer2 = handPlayer2.add(deck.draw())).deal()
  }
}

@ObsoleteCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor,
    deck: Deck = Deck()): GameActor = GlobalScope.actor {

  val players by lazy {
    arrayOf(player1, player2)
  }

  var gameState: GameState = GameState(deck=deck)



  for (msg in channel) when (msg) {
    is GameMessage.GameStateQuery -> msg.deferred.complete(gameState)


    is GameMessage.PlayRound -> {
      gameState = gameState.deal()
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
