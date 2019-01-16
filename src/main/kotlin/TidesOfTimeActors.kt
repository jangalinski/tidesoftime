package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.game.*
import com.github.jangalinski.tidesoftime.player.PlayerMessage
import com.github.jangalinski.tidesoftime.player.RandomCardSelection
import com.github.jangalinski.tidesoftime.player.getState
import com.github.jangalinski.tidesoftime.player.player
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlin.math.round


typealias GameScope = CoroutineScope

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun main() = runBlocking {
  val p1 = player("Heinz", RandomCardSelection)
  //val p1 = player("Heinz", ConsoleSelectionStrategy)
  val p2 = player("Uwe", RandomCardSelection)


  with(game(p1, p2)) {
    // Round #1
    playRound()
    getState().await().let { println("gamestate: $it") }
    repeat(5) { cardToKingdom() }
    countPoints()
    roundEnded()

    // Round #2
    playRound()
    getState().await().let { println("gamestate: $it") }
    repeat(5) { cardToKingdom() }
    countPoints()
    roundEnded()

    // Round #3
    playRound()
    getState().await().let { println("gamestate: $it") }
    repeat(5) { cardToKingdom() }
    countPoints()

    // Round over
    getState().await().also { println("gamestate: $it") }.points.also { println("""
      final score:
      p1: ${it.first}
      p2: ${it.second}
    """.trimIndent()) }
    //close()
  }

  p1.getState().await().let { println("p1 $it") }
  p2.getState().await().let { println("p2 $it") }
}
