package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.*
import com.github.jangalinski.tidesoftime.player.*
import game.GameState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*

sealed class GameMessage {
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()
  object CountPoints : GameMessage()
  object RoundEnded : GameMessage()
  data class GameStateQuery(val deferred: CompletableDeferred<GameState>) : GameMessage()
}

typealias GameActor = SendChannel<GameMessage>
suspend fun GameActor.cardToKingdom() = this.send(GameMessage.CardToKingdom)
suspend fun GameActor.playRound()  = this.send(GameMessage.PlayRound)
suspend fun GameActor.countPoints() = this.send(GameMessage.CountPoints)
suspend fun GameActor.roundEnded() = this.send(GameMessage.RoundEnded)
suspend fun GameActor.getState(): CompletableDeferred<GameState> = CompletableDeferred<GameState>().also { this.send(GameMessage.GameStateQuery(it)) }

infix fun GameState.consume(command: (GameState) -> Any) : GameState {
  command(this)
  return this
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor
): GameActor = GlobalScope.actor {

  var state = GameState()

  val deck = Card.shuffled().toMutableList()

  val deckRef = createDeck(deck)

  suspend fun cardToKingdom(state: GameState)= state.updatePlayer(
          player1.chooseCardToPlay(state.visibleForPlayer1).await().let(state.player1::playCard),
          player2.chooseCardToPlay(state.visibleForPlayer2).await().let(state.player2::playCard)
  )

  suspend fun markRelicOfPast(state: GameState)= state.updatePlayer(
          player1.markRelicOfPast(state.visibleForPlayer1).await().let(state.player1::markRelicOfPast),
          player2.markRelicOfPast(state.visibleForPlayer2).await().let(state.player2::markRelicOfPast)
  )

  suspend fun destroyKingdomCard(state: GameState)= state.updatePlayer(
          player1.destroyKingdomCard(state.visibleForPlayer1).await().let(state.player1::destroyKingdomCard),
          player2.destroyKingdomCard(state.visibleForPlayer2).await().let(state.player2::destroyKingdomCard)
  )

  for (msg in channel)
    state = when (msg) {
      is GameMessage.GameStateQuery -> state consume { msg.deferred.complete(it) }

      is GameMessage.PlayRound -> state.deal(deckRef)

      is GameMessage.CardToKingdom -> state
              .let{ cardToKingdom(it) }
              .swapHands()

      is GameMessage.RoundEnded -> state
              .let { markRelicOfPast(it) }
              .let { destroyKingdomCard(it) }
              .takeCardsFromKingdomToHand()

      is GameMessage.CountPoints -> state.updateScore()
    }
}