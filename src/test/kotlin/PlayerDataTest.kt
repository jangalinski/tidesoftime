package com.github.jangalinski.tidesoftime

import com.github.jangalinski.tidesoftime.deck.Card
import com.github.jangalinski.tidesoftime.game.KingdomCard
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
internal class PlayerDataTest {

  val player  = PlayerData()

  @Test
  internal fun `at the end of a round, one card is removed and one is kept, the rest goes back to the hand`() {
    with(player) {
      deal(Card.DER_PASS_DES_ALTEN_MANNES)
      deal(Card.DAS_KOENIGSNEST)
      deal(Card.DAS_BAD_DER_GOETTER)
      deal(Card.DIE_BLUTTRAENENQUELLE)
      deal(Card.DIE_ZITADELLE_DER_PROPHETEN)

      hand.forEach { toKingdom(it) }
    }
    assertThat(player.hand).isEmpty()
    assertThat(player.kingdom).hasSize(5)

    //player.finishRound(Card.DER_PASS_DES_ALTEN_MANNES, Card.DAS_KOENIGSNEST)

    assertThat(player.hand).containsExactly(Card.DAS_BAD_DER_GOETTER, Card.DIE_BLUTTRAENENQUELLE, Card.DIE_ZITADELLE_DER_PROPHETEN)
    assertThat(player.discarded).containsExactly(Card.DAS_KOENIGSNEST)
    assertThat(player.kingdom).containsExactly(KingdomCard(Card.DER_PASS_DES_ALTEN_MANNES, true))

    player.deal(Card.DAS_DACH_DER_WELT)
    player.deal(Card.DAS_AUGE_DES_NORDENS)

    assertThat(player.hand).containsExactly(Card.DAS_BAD_DER_GOETTER, Card.DIE_BLUTTRAENENQUELLE, Card.DIE_ZITADELLE_DER_PROPHETEN, Card.DAS_DACH_DER_WELT, Card.DAS_AUGE_DES_NORDENS)
    player.hand.forEach { player.toKingdom(it) }

    assertThat(player.hand).isEmpty()
    assertThat(player.kingdom).hasSize(6)

    //player.finishRound(Card.DAS_BAD_DER_GOETTER, Card.DIE_BLUTTRAENENQUELLE)
  }

  @Test
  internal fun `a player can move a card from hand to kingdom`() {
    player.deal(Card.DER_PASS_DES_ALTEN_MANNES)

    assertThat(player.hand.first()).isEqualTo(Card.DER_PASS_DES_ALTEN_MANNES)
    assertThat(player.kingdom).isEmpty()

    player.toKingdom(Card.DER_PASS_DES_ALTEN_MANNES)

    assertThat(player.hand).isEmpty()
    assertThat(player.kingdom.first().card).isEqualTo(Card.DER_PASS_DES_ALTEN_MANNES)
  }

  @Test
  internal fun `a player can be dealt cards`() {
    player.deal(Card.DER_PASS_DES_ALTEN_MANNES)
    player.deal(Card.DAS_KOENIGSNEST)

    assertThat(player.hand).hasSize(2)
    assertThat(player.hand).containsExactly(Card.DER_PASS_DES_ALTEN_MANNES, Card.DAS_KOENIGSNEST)
  }

  @Test
  internal fun `a new player has no hand and no kingdom`() {
    assertThat(player.kingdom).isEmpty()
    assertThat(player.hand).isEmpty()
    assertThat(player.discarded).isEmpty()
  }
}