package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.DeckMessage
import com.github.jangalinski.tidesoftime.deal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

typealias Score = Pair<Int, Int>

data class ImmutablePlayerData (val hand: Hand = Hand(), val kingdom: Kingdom = Kingdom()) {
    fun deal(card: Card) = copy(hand = hand.add(card))

    fun playCard(cardOfPlayer: Card) = copy(hand = hand.remove(card = cardOfPlayer), kingdom = kingdom.add(cardOfPlayer))

    fun replaceHand(newHand: Hand) = copy(hand = newHand)

    fun markRelicOfPast(card: Card) = copy(kingdom = kingdom.mark(card))

    fun destroyKingdomCard(card: Card): ImmutablePlayerData = copy(kingdom = kingdom.remove(card))

    fun takeCardsFromKingdomToHand() = copy(kingdom = Kingdom(kingdom.marked().toSet()), hand = Hand(kingdom.unmarked().map { it.card }.toSet()))
}

infix fun GameState.peek(command: suspend CoroutineScope.(GameState) -> Any): GameState {
    val state = this
    runBlocking { command(state) }
    return this
}

infix fun GameState.map(block: suspend CoroutineScope.(GameState) -> GameState): GameState {
    val state = this
    return runBlocking { block(state) }
}

data class GameStateView(
        val own: ImmutablePlayerData,
        val otherKingdom: Kingdom
)

data class GameState(
        val player1: ImmutablePlayerData = ImmutablePlayerData(),
        val player2: ImmutablePlayerData = ImmutablePlayerData(),
        val points: Score = 0 to 0
) {

    val visibleForPlayer1: GameStateView by lazy {
        GameStateView(own = player1, otherKingdom = player2.kingdom)
    }

    val visibleForPlayer2: GameStateView by lazy {
        GameStateView(own = player2, otherKingdom = player1.kingdom)
    }

    private val handSize : Int by lazy {
        arrayOf(player1.hand, player2.hand).map { it.size }.distinct().single()
    }

    suspend fun deal(dealer: SendChannel<DeckMessage>): GameState {
        if(handSize == Hand.SIZE) {
            return this
        }


        println(player1)
        println(player2)
        return copy(player1 = player1.deal(dealer.deal()), player2 = player2.deal(dealer.deal()))
                .deal(dealer) // recursive call until every player has 5 cards on his hand
    }

    fun swapHands() = copy(player1 = player1.replaceHand(player2.hand), player2 = player2.replaceHand(player1.hand))

    fun takeCardsFromKingdomToHand() = copy(player1 = player1.takeCardsFromKingdomToHand(), player2 = player2.takeCardsFromKingdomToHand())


    fun updateScore(): GameState {
        val (r1, r2) = countPoints(player1.kingdom, player2.kingdom)

        println("""
        updating score! adding following points to the score..
        player1: ${points.first} + ${r1.sum} = ${points.first + r1.sum}
        player2: ${points.second} + ${r2.sum}  = ${points.second+ r2.sum}
    """.trimIndent())

        return copy(points = points.first + r1.sum to points.second + r2.sum)
    }

    fun updatePlayer(playerData1: ImmutablePlayerData, playerData2: ImmutablePlayerData) = copy(player1 = playerData1, player2 = playerData2)
}