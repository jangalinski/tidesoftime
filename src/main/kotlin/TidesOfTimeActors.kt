package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.game.GameMessage
import com.github.jangalinski.tidesoftime.game.game
import com.github.jangalinski.tidesoftime.player.ConsoleSelectionStrategy
import com.github.jangalinski.tidesoftime.player.RandomCardSelection
import com.github.jangalinski.tidesoftime.player.player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking


typealias GameScope = CoroutineScope

@ObsoleteCoroutinesApi
fun main() = runBlocking<Unit> {
  val p1 = player("Heinz", RandomCardSelection)
  //val p1 = player("Heinz", ConsoleSelectionStrategy)
  val p2 = player("Uwe", RandomCardSelection)

  with(game(p1, p2)) {
    send(GameMessage.Deal)

    send(GameMessage.PrintState)

    send(GameMessage.CardToKingdom)
    send(GameMessage.CardToKingdom)
    send(GameMessage.CardToKingdom)
    send(GameMessage.CardToKingdom)
    send(GameMessage.CardToKingdom)

    send(GameMessage.PrintState)

    close()
  }
}
