package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.deck.Card
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HandTest {
  var hand = Hand()

  @Test
  internal fun `init empty`() {
    assertThat(hand).isEmpty()
  }

  @Test
  internal fun `can add card`() {
    assertThat(hand.add(Card.DER_PASS_DES_ALTEN_MANNES)).containsExactly(Card.DER_PASS_DES_ALTEN_MANNES)
  }

  @Test
  internal fun `can remove card`() {
    hand = hand.add(Card.DER_PASS_DES_ALTEN_MANNES).add(Card.DAS_KOENIGSNEST)

    assertThat(hand.remove(Card.DAS_KOENIGSNEST)).containsExactly(Card.DER_PASS_DES_ALTEN_MANNES)
  }
}