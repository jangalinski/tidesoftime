package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.createDeck
import com.github.jangalinski.tidesoftime.player.PlayerActor
import com.github.jangalinski.tidesoftime.player.chooseCardToPlay
import com.github.jangalinski.tidesoftime.player.destroyKingdomCard
import com.github.jangalinski.tidesoftime.player.markRelicOfPast
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

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

infix fun GameState.peek(command: suspend CoroutineScope.(GameState) -> Any): GameState {
  val state = this
  runBlocking { command(state) }
  return this
}

infix fun GameState.map(block: suspend CoroutineScope.(GameState) -> GameState): GameState {
  val state = this
  return runBlocking { block(state) }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor
): GameActor = GlobalScope.actor {

  var state = GameState()

  val deckRef = createDeck(Card.shuffled())

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
    state = with(state) {
      when (msg) {
        is GameMessage.GameStateQuery -> peek { msg.deferred.complete(it) }

        is GameMessage.PlayRound -> map { it.deal(deckRef) }

        is GameMessage.CardToKingdom -> map { cardToKingdom(it) }
                .map { it.swapHands() }

        is GameMessage.RoundEnded -> map { markRelicOfPast(it) }
                .map { destroyKingdomCard(it) }
                .map { it.takeCardsFromKingdomToHand() }

        is GameMessage.CountPoints -> map { it.updateScore() }
      }
    }
}