package com.github.jangalinski.tidesoftime.player

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.Player
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.Kingdom
import com.github.jangalinski.tidesoftime.player.PlayerMessage.*
import game.GameStateView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

typealias PlayerActor = SendChannel<PlayerMessage>
suspend fun PlayerActor.chooseCardToPlay(gameState: GameStateView): CompletableDeferred<Card>  = CompletableDeferred<Card>().also{this.send(ChooseCardToPlay(gameState, it))}
suspend fun PlayerActor.markRelicOfPast(gameState: GameStateView): CompletableDeferred<Card>  = CompletableDeferred<Card>().also{this.send(MarkRelicOfPast(gameState, it))}
suspend fun PlayerActor.destroyKingdomCard(gameState: GameStateView): CompletableDeferred<Card> = CompletableDeferred<Card>().also{this.send(DestroyKingdomCard(gameState, it))}
suspend fun PlayerActor.getState(): CompletableDeferred<PlayerState>   = CompletableDeferred<PlayerState>().also{this.send(PlayerStateQuery(it))}

sealed class PlayerMessage {
  data class ChooseCardToPlay(val gameState: GameStateView, val cardToPlay: CompletableDeferred<Card>) : PlayerMessage()
  data class MarkRelicOfPast(val gameState: GameStateView, val markedKingdomCard: CompletableDeferred<Card>) : PlayerMessage()
  data class DestroyKingdomCard(val gameState: GameStateView, val discardedCard: CompletableDeferred<Card>) : PlayerMessage()
  data class PlayerStateQuery(val deferred: CompletableDeferred<PlayerState>) : PlayerMessage()
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
    strategy: PlayerStrategy
): PlayerActor = GlobalScope.actor {

  var state = PlayerState()

  for (msg in channel) when (msg) {
    is PlayerStateQuery -> msg.deferred.complete(state)

    is ChooseCardToPlay -> {
      val cardToPlay = strategy.playHandCard(msg.gameState.own.hand)
      println("${msg.gameState.own.hand.size}: $name plays $cardToPlay")
      msg.cardToPlay.complete(cardToPlay)
    }

    is MarkRelicOfPast -> {
      val markedKingdomCard = strategy.keepKingdomCard(msg.gameState.own.kingdom)
      println("$name marks $markedKingdomCard as relic of the past")
      msg.markedKingdomCard.complete(markedKingdomCard)
    }

    is DestroyKingdomCard -> {
      val markedKingdomCard = strategy.destroyKingdomCard(msg.gameState.own.kingdom)
      println("$name destroys $markedKingdomCard from his kingdom")
      msg.discardedCard.complete(markedKingdomCard)
    }
  }
}
