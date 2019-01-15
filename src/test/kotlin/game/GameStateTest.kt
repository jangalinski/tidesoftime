package game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.createDeck
import com.github.jangalinski.tidesoftime.game.GameState
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.PlayerData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GameStateTest {


  @ExperimentalCoroutinesApi
  @Test
  internal fun `deal until both players have 5 cards`() = runBlocking {
    val gameState = GameState(deck = createDeck(Card.values().asList()))

    val (_, player1, player2) = gameState.deal()

    assertThat(player1.hand).hasSize(Hand.SIZE)
    assertThat(player2.hand).hasSize(Hand.SIZE)

    val (_, player1_2, player2_2) = gameState.copy(player1 = PlayerData(), player2 = PlayerData()).deal()

    assertThat(player1_2.hand).hasSize(Hand.SIZE)
    assertThat(player2_2.hand).hasSize(Hand.SIZE)
  }
}