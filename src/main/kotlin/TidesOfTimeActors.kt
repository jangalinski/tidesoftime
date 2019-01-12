package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.game.GameMessage
import com.github.jangalinski.tidesoftime.game.game
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import com.github.jangalinski.tidesoftime.player.RandomCardSelection
import com.github.jangalinski.tidesoftime.player.player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


typealias GameScope = CoroutineScope

@ObsoleteCoroutinesApi
fun main() = runBlocking<Unit> {
  val p1 = player("Heinz", RandomCardSelection)
  //val p1 = player("Heinz", ConsoleSelectionStrategy)
  val p2 = player("Uwe", RandomCardSelection)

  val game = game(p1,p2)


  game.send(GameMessage.PlayRound)


  delay(2000)
  with(game(p1, p2)) {
    send(GameMessage.PlayRound)

    GameMessage.GameStateQuery.sendTo(this).invokeOnCompletion { println("gamestate: $it") }

    //close()
  }

  PlayerMessage.PlayerStateQuery.sendTo(p1).invokeOnCompletion { println("p1: $it") }
  PlayerMessage.PlayerStateQuery.sendTo(p2).await().let{ println("p2: $it") }

}
