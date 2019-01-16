package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.*
import com.github.jangalinski.tidesoftime.player.*
import game.GameState
import game.Score
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*

typealias GameActor = SendChannel<GameMessage>

suspend fun GameActor.cardToKingdom() {
  this.send(GameMessage.CardToKingdom)
}

suspend fun GameActor.playRound() {
  this.send(GameMessage.PlayRound)
}

suspend fun GameActor.countPoints() {
  this.send(GameMessage.CountPoints)
}

suspend fun GameActor.roundEnded() {
  this.send(GameMessage.RoundEnded)
}

suspend fun GameActor.getScore(): CompletableDeferred<Score> {
  val deferred = CompletableDeferred<Score>()
  this.send(GameMessage.GetScore(deferred))
  return deferred
}

suspend fun GameActor.getState(): CompletableDeferred<GameState> {
  val deferred = CompletableDeferred<GameState>()
  this.send(GameMessage.GameStateQuery(deferred))
  return deferred
}





sealed class GameMessage {

  object CardToKingdom : GameMessage()

  object PlayRound : GameMessage()

  object CountPoints : GameMessage()

  object RoundEnded : GameMessage()

  data class GetScore(val deferred: CompletableDeferred<Score>) : GameMessage()

  data class GameStateQuery(val deferred: CompletableDeferred<GameState>) : GameMessage()

}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor): GameActor = GlobalScope.actor {

  var state = GameState(deck = createDeck(Card.shuffled()))

  for (msg in channel) when (msg) {
    is GameMessage.GameStateQuery -> msg.deferred.complete(state)

    is GameMessage.PlayRound -> {
      state = state.deal()
    }

    is GameMessage.CardToKingdom -> {
      state = state.updatePlayer(
              player1.chooseCardToPlay(state.visibleForPlayer1).await().let(state.player1::playCard),
              player2.chooseCardToPlay(state.visibleForPlayer2).await().let(state.player1::playCard)
      )
    }

    is GameMessage.CountPoints -> {
      state = state.updateScore()
    }

    is GameMessage.RoundEnded -> {
      state = state.updatePlayer(
              player1.markRelicOfPast(state.visibleForPlayer1).await().let(state.player1::markRelicOfPast),
              player2.markRelicOfPast(state.visibleForPlayer2).await().let(state.player1::markRelicOfPast)
      ).updatePlayer(
              player1.destroyKindomCard(state.visibleForPlayer1).await().let(state.player1::destroyKingdomCard),
              player2.destroyKindomCard(state.visibleForPlayer2).await().let(state.player2::destroyKingdomCard)
      ).updatePlayer(
              state.player1.kingdomToHand(),
              state.player2.kingdomToHand()
      )
    }

    is GameMessage.GetScore -> {
      msg.deferred.complete(state.points)
    }
  }

}