package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.*
import com.github.jangalinski.tidesoftime.player.PlayerActor
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor


typealias GameActor = SendChannel<GameMessage>

typealias Score = Pair<Int, Int>

sealed class GameMessage {
  object PrintState : GameMessage()
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()
  data class CountPoints(val deferred: CompletableDeferred<Pair<Int, Int>>) : GameMessage() {
    companion object {
      suspend fun sendTo(game: GameActor): CompletableDeferred<Pair<Int, Int>> = with(CompletableDeferred<Pair<Int, Int>>()){
        game.send(CountPoints(this))
        return this
      }
    }
  }

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
    val deck: DeckActor,
    val handPlayer1: Hand = Hand(), val kingdomPlayer1: Kingdom = Kingdom(),
    val handPlayer2: Hand = Hand(), val kingdomPlayer2: Kingdom = Kingdom(),
    val points: Pair<Int, Int> = 0 to 0
) {

  val handSize : Int by lazy {
    arrayOf(handPlayer1, handPlayer2).map { it.size }.distinct().single()
  }

  suspend fun deal(): GameState {
    if(handSize == Hand.SIZE) {
      return this
    }
    return copy(handPlayer1 = handPlayer1.add(deck.receive()), handPlayer2 = handPlayer2.add(deck.receive())).deal()
  }

  fun playCards(cardOfPlayer1: Card, cardOfPlayer2: Card): GameState {
    return copy(
            handPlayer1 = handPlayer1.remove(cardOfPlayer1), kingdomPlayer1 = kingdomPlayer1.add(cardOfPlayer1),
            handPlayer2 = handPlayer2.remove(cardOfPlayer2), kingdomPlayer2 = kingdomPlayer2.add(cardOfPlayer2)
    )
  }

  fun swapHands(): GameState {
    return copy(handPlayer1 = handPlayer1, handPlayer2 = handPlayer2)
  }

  fun updateScore(): GameState {
    val (r1, r2) = countPoints(kingdomPlayer1, kingdomPlayer2)

    println("""
        updating score! adding following points to the score..
        player #1: ${r1.sum}
        player #2: ${r2.sum}
    """.trimIndent())

    return copy(points = points.first + r1.sum to points.second + r2.sum)
  }
}

@ObsoleteCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor,
    deck: DeckActor = deckActor(true)): GameActor = GlobalScope.actor {

  val players by lazy {
    arrayOf(player1, player2)
  }

  var gameState = GameState(deck=deck)

  for (msg in channel) when (msg) {
    is GameMessage.GameStateQuery -> msg.deferred.complete(gameState)

    is GameMessage.PlayRound -> {
      gameState = gameState.deal()
    }

    is GameMessage.CardToKingdom -> {
      val cardOfPlayer1 = PlayerMessage.ChooseCardToPlay.sendTo(player1, gameState.handPlayer1, gameState.kingdomPlayer1, gameState.kingdomPlayer2).await()
      val cardOfPlayer2 = PlayerMessage.ChooseCardToPlay.sendTo(player2, gameState.handPlayer2, gameState.kingdomPlayer2, gameState.kingdomPlayer1).await()

      gameState = gameState.let { it.playCards(cardOfPlayer1, cardOfPlayer2) } .let { it.swapHands() }
    }

    is GameMessage.CountPoints -> {
      gameState = gameState.updateScore()
      msg.deferred.complete(gameState.points)
    }
  }

}


private suspend fun deal(p: PlayerActor, deck: Deck): CompletableDeferred<Int> = with(CompletableDeferred<Int>()) {
  p.send(PlayerMessage.DealCard(deck.draw(), this))
  return this
}
