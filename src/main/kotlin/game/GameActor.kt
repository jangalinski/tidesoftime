package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.*
import com.github.jangalinski.tidesoftime.player.PlayerActor
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*

typealias GameActor = SendChannel<GameMessage>

typealias Score = Pair<Int, Int>

sealed class GameMessage {
  object CardToKingdom : GameMessage()
  object PlayRound : GameMessage()
  object CountPoints : GameMessage()

  object RoundEnded : GameMessage()

  data class GetScore(val deferred: CompletableDeferred<Score>) : GameMessage() {
    companion object {
      suspend fun sendTo(game: GameActor): CompletableDeferred<Score> = with(CompletableDeferred<Score>()){
        game.send(GetScore(this))
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
    assert(hand.contains(cardOfPlayer))
    return copy(hand = hand.remove(card = cardOfPlayer), kingdom = kingdom.add(cardOfPlayer))
  }

  fun replaceHand(newHand: Hand) : PlayerData {
    return copy(hand = newHand)
  }

  fun markRelicOfPast(card: Card): PlayerData {
    return copy(kingdom = kingdom.mark(card))
  }

  fun destroyKingdomCard(card: Card): PlayerData {
    return copy(kingdom = kingdom.remove(card))
  }

  fun kingdomToHand(): PlayerData {
    return copy(kingdom = Kingdom(kingdom.marked().toSet()), hand = Hand(kingdom.unmarked().map { it.card }.toSet()))
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

  fun updatePlayer(newPlayerData1: PlayerData, newPlayerData2: PlayerData): GameState {
    return copy(player1 = newPlayerData1, player2 = newPlayerData2)
  }
}

@ObsoleteCoroutinesApi
fun game(
    player1: PlayerActor,
    player2: PlayerActor): GameActor = GlobalScope.actor {

  var gameState = GameState(deck = createDeck(Card.shuffled()))

  for (msg in channel) when (msg) {
    is GameMessage.GameStateQuery -> msg.deferred.complete(gameState)

    is GameMessage.PlayRound -> {
      gameState = gameState.deal()
    }

    is GameMessage.CardToKingdom -> {
      suspend fun playCard(player: PlayerActor, playerData: PlayerData, otherPlayerKingdom: Kingdom): PlayerData {
        val card = PlayerMessage.ChooseCardToPlay.sendTo(player, playerData.hand, playerData.kingdom, otherPlayerKingdom).await()
        return playerData.playCard(card)
      }

      val newPlayerData1 = playCard(player1, gameState.player1, gameState.player2.kingdom)
      val newPlayerData2 = playCard(player2, gameState.player2, gameState.player1.kingdom)

      gameState = gameState.updatePlayer(newPlayerData1, newPlayerData2).swapHands()
    }

    is GameMessage.CountPoints -> {
      gameState = gameState.updateScore()
    }

    is GameMessage.RoundEnded -> {
      suspend fun markRelicOfPast(player: PlayerActor, playerData: PlayerData): PlayerData {
        val cardToMarkAsRelicOfPast = PlayerMessage.MarkRelicOfPast.sendTo(player, playerData.kingdom).await()
        return playerData.markRelicOfPast(cardToMarkAsRelicOfPast)
      }

      suspend fun destroyKingdomCard(player: PlayerActor, playerData: PlayerData): PlayerData {
        val cardToMarkAsRelicOfPast = PlayerMessage.DestroyKingdomCard.sendTo(player, playerData.kingdom).await()
        return playerData.destroyKingdomCard(cardToMarkAsRelicOfPast)
      }

      val newPlayerData1 = destroyKingdomCard(player2, markRelicOfPast(player1, gameState.player1)).kingdomToHand()
      val newPlayerData2 = destroyKingdomCard(player2, markRelicOfPast(player2, gameState.player2)).kingdomToHand()
      gameState = gameState.updatePlayer(newPlayerData1, newPlayerData2)
    }

    is GameMessage.GetScore -> {
      msg.deferred.complete(gameState.points)
    }
  }

}