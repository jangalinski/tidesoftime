package game

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.Deck
import com.github.jangalinski.tidesoftime.game.GameState
import com.github.jangalinski.tidesoftime.game.Hand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GameStateTest {


  @Test
  internal fun `deal until both players have 5 cards`() {
    var gameState = GameState(deck = Deck(Card.values().toList(), shuffle = false))

    val (deck, hand1, _, hand2, _) = gameState.deal()

    assertThat(deck.remaining()).isEqualTo(8)

    assertThat(hand1).hasSize(Hand.SIZE)
    assertThat(hand2).hasSize(Hand.SIZE)
  }
}