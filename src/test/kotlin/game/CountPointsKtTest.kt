package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.deck.Card
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CountPointsKtTest {

  @Test
  internal fun `missing symbol - a(3) b(0)`() {
    val (r1,r2) = countPoints(
        Kingdom().add(Card.DAS_AUGE_DES_NORDENS),
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(12)
    assertThat(r1.points).hasSize(1)
    assertThat(r2.sum).isEqualTo(0)
  }

  @Test
  internal fun `points per symbol`() {
    val (r1,r2) = countPoints(
        Kingdom()
            .add(Card.DAS_BAD_DER_GOETTER)
            .add(Card.DIE_FUSSFESSELN_DES_DSCHINNS),
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(3)
    assertThat(r2.sum).isEqualTo(0)
  }
  @Test
  internal fun `points per symbol two towers and double`() {
    val (r1,r2) = countPoints(
        Kingdom()
            .add(Card.DAS_BAD_DER_GOETTER) // F-T
            .add(Card.DIE_FUSSFESSELN_DES_DSCHINNS) // T-H
            .add(Card.DER_PASS_DES_ALTEN_MANNES) // T
            .add(Card.DAS_DACH_DER_WELT), // double
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(12)
    assertThat(r2.sum).isEqualTo(0)
  }

  @Test
  internal fun `empty kingdoms have both zero points`() {
    val (r1,r2) = countPoints(Kingdom(), Kingdom())

    assertThat(r1.sum).isEqualTo(0)
    assertThat(r2.sum).isEqualTo(0)
  }

  @Test
  internal fun `set scroll forest empty`() {
    var (r1,_) = countPoints(
        Kingdom()
            .add(Card.DIE_HIMMELSSAEULEN), // double
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(0)
  }

  @Test
  internal fun `set scroll forest 5 points`() {
    var (r1,r2) = countPoints(
        Kingdom()
            .add(Card.DIE_GROSSE_BIBLIOTHEK_VON_AHN)
            .add(Card.DER_GOLDENE_TEMPELTURM)
            .add(Card.DIE_HIMMELSSAEULEN),
        Kingdom()
    )

    assertThat(r1.points[Card.DIE_HIMMELSSAEULEN]).isEqualTo(5)
  }

  @Test
  internal fun `majority of scrolls - no`() {
    var (r1,_) = countPoints(
        Kingdom()
            .add(Card.DER_PASS_DES_ALTEN_MANNES),
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(0)
  }

  @Test
  internal fun `majority of scrolls - yes`() {
    var (r1,_) = countPoints(
        Kingdom()
            .add(Card.DIE_ZITADELLE_DER_PROPHETEN)
            .add(Card.DER_PASS_DES_ALTEN_MANNES),
        Kingdom()
    )

    assertThat(r1.sum).isEqualTo(7)
  }
}