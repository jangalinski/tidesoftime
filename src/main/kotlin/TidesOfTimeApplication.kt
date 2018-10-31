package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.game.Game


fun main() {
  val game = Game(
      player1 = Player("random1", RandomCardSelection),
      player2 = Player("random2", RandomCardSelection)
  )
  println("new game begins: $game")
// round 1
  game.deal(5)

  println("dealt: $game")
  game.takeTurns()
  game.countPoints()
  game.discard()
  game.keep()

// round 2
  game.takeCardsBack()
  game.deal(2)

  game.takeTurns()
  game.countPoints()
  game.discard()
  game.keep()

// round 3
  game.takeCardsBack()
  game.deal(2)

  game.takeTurns()
  game.countPoints()
}