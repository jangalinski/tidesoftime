package game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.createDeck
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.remainingCards
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
internal class GameStateTest {


  @Test
  internal fun `deal until both players have 5 cards`() {
    val gameState = GameState()

    val cards = Card.values().asList()
    val dealer = createDeck(cards, true)

    val (player1, player2) = runBlocking { gameState.deal(dealer) }

    assertThat(player1.hand).hasSize(Hand.SIZE)
    assertThat(player2.hand).hasSize(Hand.SIZE)
    assertThat(runBlocking { dealer.remainingCards() }).containsAll(cards - player1.hand - player2.hand)
  }
}