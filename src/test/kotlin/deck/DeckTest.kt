package com.github.jangalinski.tidesoftime.deck

import com.github.jangalinski.tidesoftime.deck.Card
import com.github.jangalinski.tidesoftime.deck.Deck
import com.github.jangalinski.tidesoftime.deck.Symbol
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

internal class DeckTest {

  private var deck = Deck(shuffle = false)

  @Test
  internal fun `there are 18 cards in a new deck, 3 of each suit`() {
    assertThat(deck.remaining()).isEqualTo(18)

    with(Card.ordered()) {
      val soft = SoftAssertions()

      for(suit in Symbol.values()) {
        soft.assertThat(filter { it.symbol == suit })
            .`as`("expected 3 of $suit")
            .hasSize(3)
      }
      soft.assertAll()
    }
  }

  @Test
  internal fun `draw first card`() {
    val card = deck.draw()

    assertThat(deck.remaining()).isEqualTo(17)
    assertThat(card).isEqualTo(Card.DAS_BAD_DER_GOETTER)
  }

  @Test
  internal fun `cannot draw more than 18`() {
    assertThatThrownBy { for (x in 1..19) {
      deck.draw()
    } }.isInstanceOf(NoSuchElementException::class.java)
  }
}