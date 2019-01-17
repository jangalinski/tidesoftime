package com.github.jangalinski.tidesoftime.playground

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking


typealias PlayerActor = SendChannel<Message>

data class Player(val name: String, val state: String)

sealed class Message {

  sealed class SwapCase : Message() {
    data class Init(val other: PlayerActor) : SwapCase()
    data class SendAndReceive(val callerState: String, val receiverState: CompletableDeferred<String>) : SwapCase()
  }

  data class GetPlayer(val deferred: CompletableDeferred<Player>) : Message()
}



fun main() = runBlocking {

  fun createPlayer(name: String, initialState: String = name): PlayerActor = actor {
    var player = Player(name, initialState)

    consumeEach { msg -> when (msg) {

      is Message.SwapCase -> when (msg) {
        is Message.SwapCase.Init -> with(CompletableDeferred<String>()) {
          msg.other.send(Message.SwapCase.SendAndReceive(player.state, this))
          player = player.copy(state = this.await())
        }

        is Message.SwapCase.SendAndReceive -> {
          msg.receiverState.complete(player.state)
          player = player.copy(state = msg.callerState)
        }
      }

      is Message.GetPlayer -> msg.deferred.complete(player)
    }
  }}


  val p1 = createPlayer("p1", "foo")
  val p2 = createPlayer("p2", "bar")


  suspend fun printPlayers() = arrayOf(p1,p2).map { player -> with(CompletableDeferred<Player>()) {
    player.send(Message.GetPlayer(this))

    this.await()
  } }.forEach{ println(it)}


  printPlayers()

  p1.send(Message.SwapCase.Init(p2))

  printPlayers()
//p1.sendTo(Message.InitSwap)

}