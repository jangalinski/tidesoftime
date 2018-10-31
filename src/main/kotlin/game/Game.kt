package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Player
import com.github.jangalinski.tidesoftime.PlayerData
import com.github.jangalinski.tidesoftime.deck.Deck

data class Game(
    val player1: Player,
    val player2: Player,
    val playerData1: PlayerData = PlayerData(),
    val playerData2: PlayerData = PlayerData(),
    val deck: Deck = Deck(),
    var points : Pair<Int,Int> = 0 to 0
) {

  fun deal(number: Int) = repeat(number) {
    playerData1.deal(deck.draw())
    playerData2.deal(deck.draw())
  }

  fun takeTurns() {
    for (turn in 1..5) {
      player1.strategy.addHandCardToKingdom(playerData1)
      player2.strategy.addHandCardToKingdom(playerData2)
      swapHands()
      println("after card $turn: $this")
    }
  }

  fun discard() {
    player1.strategy.chooseKingdomCardToDiscard(playerData1)
    player2.strategy.chooseKingdomCardToDiscard(playerData2)
  }

  fun keep() {
    player1.strategy.chooseKingdomCardToKeep(playerData1)
    player2.strategy.chooseKingdomCardToKeep(playerData2)
  }

  fun takeCardsBack() {
    playerData1.kingdom.unmarked().forEach { playerData1.fromKingdom(it.card) }
    playerData2.kingdom.unmarked().forEach { playerData2.fromKingdom(it.card) }
  }

  fun countPoints() {
    val (r1,r2) = countPoints(playerData1.kingdom, playerData2.kingdom)

    println("""

      Points player 1: ${r1.sum}
      Points player 2: ${r2.sum}
    """.trimIndent())

    points = points.first + r1.sum to points.second + r2.sum

    println("""

      $r1

      $r2

      Game result: $points

    """.trimIndent())
  }

  fun swapHands() {

    val h1 = playerData1.hand.copy()

    playerData1.hand = playerData2.hand
    playerData2.hand = h1
    println("== swapped hands")
  }

}