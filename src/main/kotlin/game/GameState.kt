package game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.DeckRef
import com.github.jangalinski.tidesoftime.deal
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.Kingdom
import com.github.jangalinski.tidesoftime.game.countPoints


typealias Score = Pair<Int, Int>

data class ImmutablePlayerData (val hand: Hand = Hand(), val kingdom: Kingdom = Kingdom()) {
    fun deal(card: Card): ImmutablePlayerData {
        return copy(hand = hand.add(card))
    }

    fun playCard(cardOfPlayer: Card): ImmutablePlayerData {
        assert(hand.contains(cardOfPlayer))
        return copy(hand = hand.remove(card = cardOfPlayer), kingdom = kingdom.add(cardOfPlayer))
    }

    fun replaceHand(newHand: Hand) : ImmutablePlayerData {
        return copy(hand = newHand)
    }

    fun markRelicOfPast(card: Card): ImmutablePlayerData {
        return copy(kingdom = kingdom.mark(card))
    }

    fun destroyKingdomCard(card: Card): ImmutablePlayerData {
        return copy(kingdom = kingdom.remove(card))
    }

    fun kingdomToHand(): ImmutablePlayerData {
        return copy(kingdom = Kingdom(kingdom.marked().toSet()), hand = Hand(kingdom.unmarked().map { it.card }.toSet()))
    }
}

data class GameStateView(
        val own: ImmutablePlayerData,
        val otherKingdom: Kingdom
)

data class GameState(
        val deck: DeckRef,
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

    suspend fun deal(): GameState {
        if(handSize == Hand.SIZE) {
            return this
        }

        return copy(player1 = player1.deal(deck.deal()), player2 = player2.deal(deck.deal())).deal()
    }

    fun swapHands(): GameState {
        return copy(player1 = player1.replaceHand(player2.hand), player2 = player2.replaceHand(player1.hand))
    }

    fun updateScore(): GameState {
        val (r1, r2) = countPoints(player1.kingdom, player2.kingdom)

        println("""
        updating score! adding following points to the score..
        player #1: ${r1.sum}
        player #2: ${r2.sum}
    """.trimIndent())

        return copy(points = points.first + r1.sum to points.second + r2.sum)
    }

    fun updatePlayer(newPlayerData1: ImmutablePlayerData, newPlayerData2: ImmutablePlayerData): GameState {
        return copy(player1 = newPlayerData1, player2 = newPlayerData2)
    }
}