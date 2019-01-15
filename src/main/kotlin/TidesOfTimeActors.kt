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
fun main() = runBlocking {
  val p1 = player("Heinz", RandomCardSelection)
  //val p1 = player("Heinz", ConsoleSelectionStrategy)
  val p2 = player("Uwe", RandomCardSelection)

  val game = game(p1,p2)


  game.send(GameMessage.PlayRound)


  delay(2000)
  with(game(p1, p2)) {
    // Round #1
    send(GameMessage.PlayRound)
    GameMessage.GameStateQuery.sendTo(this).await().let { println("gamestate: $it") }
    repeat(5) { send(GameMessage.CardToKingdom) }
    send(GameMessage.CountPoints)
    send(GameMessage.RoundEnded)

    // Round #2
    send(GameMessage.PlayRound)
    GameMessage.GameStateQuery.sendTo(this).await().let { println("gamestate: $it") }
    repeat(5) { send(GameMessage.CardToKingdom) }
    send(GameMessage.CountPoints)
    send(GameMessage.RoundEnded)

    // Round #3
    send(GameMessage.PlayRound)
    GameMessage.GameStateQuery.sendTo(this).await().let { println("gamestate: $it") }
    repeat(5) { send(GameMessage.CardToKingdom) }
    send(GameMessage.CountPoints)

    // Round over
    GameMessage.GameStateQuery.sendTo(this).await().let { println("gamestate: $it") }
    GameMessage.GetScore.sendTo(this).await().let { println("""
      # scoring #
      p1: ${it.first}
      p2: ${it.second}
    """.trimIndent()) }
    //close()
  }

  PlayerMessage.PlayerStateQuery.sendTo(p1).await().let { println("p1: $it") }
  PlayerMessage.PlayerStateQuery.sendTo(p2).await().let{ println("p2: $it") }

}
