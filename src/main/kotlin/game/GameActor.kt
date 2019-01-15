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
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()

  data class CountPoints(val deferred: CompletableDeferred<Score>) : GameMessage() {
    companion object {
      suspend fun sendTo(game: GameActor): CompletableDeferred<Score> = with(CompletableDeferred<Score>()){
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

data class PlayerData (val hand: Hand = Hand(), val kingdom: Kingdom = Kingdom()) {
  fun deal(card: Card): PlayerData {
    return copy(hand = hand.add(card))
  }

  fun playCard(cardOfPlayer: Card): PlayerData {
    return copy(hand = hand.remove(card = cardOfPlayer), kingdom = kingdom.add(cardOfPlayer))
  }

  fun replaceHand(newHand: Hand) : PlayerData {
    return copy(hand = newHand)
  }
}

data class GameState(
        val deck: DeckRef,
        val player1: PlayerData = PlayerData(),
        val player2: PlayerData = PlayerData(),
        val points: Score = 0 to 0
) {

  private val handSize : Int by lazy {
    arrayOf(player1.hand, player2.hand).map { it.size }.distinct().single()
  }

  suspend fun deal(): GameState {
    if(handSize == Hand.SIZE) {
      return this
    }
    return copy(player1 = player1.deal(deck.receive()), player2 = player2.deal(deck.receive())).deal()
  }

  fun playCard(cardOfPlayer1: Card, cardOfPlayer2: Card): GameState {
    return copy(player1 = player1.playCard(cardOfPlayer1), player2 = player2.playCard(cardOfPlayer2))
  }

  fun swapHands(): GameState {
    return copy(player1 = player1.replaceHand(player2.hand), player2 = player2.replaceHand(player1.hand))
  }

  fun updateScore(): GameState {
    val (r1, r2) = countPoints(player1.kingdom, player2.kingdom)

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
    deck: DeckRef = createDeck()): GameActor = GlobalScope.actor {

  var gameState = GameState(deck=deck)

  for (msg in channel) when (msg) {
    is GameMessage.GameStateQuery -> msg.deferred.complete(gameState)

    is GameMessage.PlayRound -> {
      gameState = gameState.deal()
    }

    is GameMessage.CardToKingdom -> {
      suspend fun retrieveCardToPlay(player: PlayerActor, playerData: PlayerData, otherPlayerKingdom: Kingdom): Card {
        return PlayerMessage.ChooseCardToPlay.sendTo(player, playerData.hand, playerData.kingdom, otherPlayerKingdom).await()
      }

      val cardOfPlayer1 = retrieveCardToPlay(player1, gameState.player1, gameState.player2.kingdom)
      val cardOfPlayer2 = retrieveCardToPlay(player2, gameState.player2, gameState.player1.kingdom)

      gameState = gameState.playCard(cardOfPlayer1, cardOfPlayer2).swapHands()
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
