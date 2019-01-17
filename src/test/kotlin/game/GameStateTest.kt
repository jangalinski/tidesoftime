package game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.createDeck
import com.github.jangalinski.tidesoftime.game.Hand
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class GameStateTest {


  @Test
  internal fun `deal until both players have 5 cards`() = runBlocking {
    val gameState = GameState()

    val dealer = createDeck(Card.values().asList())

    val (player1, player2) = gameState.deal(dealer)

    assertThat(player1.hand).hasSize(Hand.SIZE)
    assertThat(player2.hand).hasSize(Hand.SIZE)

    val (player1_2, player2_2) = gameState.copy(player1 = ImmutablePlayerData(), player2 = ImmutablePlayerData()).deal(dealer)

    assertThat(player1_2.hand).hasSize(Hand.SIZE)
    assertThat(player2_2.hand).hasSize(Hand.SIZE)
  }
}