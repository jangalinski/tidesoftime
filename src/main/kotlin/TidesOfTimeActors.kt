package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.game.*
import com.github.jangalinski.tidesoftime.player.RandomCardSelection
import com.github.jangalinski.tidesoftime.player.getState
import com.github.jangalinski.tidesoftime.player.player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking


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

  listOf(p1, p2).map { it.getState() }.forEachIndexed { index, state -> println("p${index + 1} ${state.await()}") }
}
