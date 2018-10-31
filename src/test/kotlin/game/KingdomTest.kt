package com.github.jangalinski.tidesoftime.game

import com.github.jangalinski.tidesoftime.deck.Card.*
import com.github.jangalinski.tidesoftime.deck.Symbol
import com.github.jangalinski.tidesoftime.deck.Symbol.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KingdomTest {

  private var kingdom = Kingdom()

  private val oneOfEach = Kingdom()
      .add(DAS_BAD_DER_GOETTER)
      .add(DER_VORHOF)
      .add(DIE_ZITADELLE_DER_PROPHETEN)
      .add(DIE_FUSSFESSELN_DES_DSCHINNS)
      .add(DER_EWIGE_PALAST)

  @BeforeEach
  internal fun setUp() {
    assertThat(oneOfEach).hasSize(5)

    Symbol.actual.forEach {
      assertThat(oneOfEach.numberOf[it]).isEqualTo(1)
    }
  }

  @Test
  internal fun `number of towers`() {
    assertThat(kingdom.add(DER_PASS_DES_ALTEN_MANNES)
        .add(DAS_KOENIGSNEST)
        .add(DAS_BAD_DER_GOETTER)
        .numberOf[Symbol.Tower])
        .isEqualTo(1)
  }

  @Test
  internal fun `double max symbols`() {
    kingdom = oneOfEach
    assertThat(kingdom.doublesMaxSymbols).isFalse()

    kingdom = kingdom.add(DAS_DACH_DER_WELT)
    assertThat(kingdom.add(DAS_DACH_DER_WELT).doublesMaxSymbols).isTrue()

    val soft = SoftAssertions()
    Symbol.actual.forEach {
      soft.assertThat(kingdom.effectiveNumberOf[it]).isEqualTo(2)
    }
    soft.assertAll()
  }

  @Test
  internal fun `number of  single symbols`() {
    assertThat(oneOfEach.numberOfSingleSymbols).isEqualTo(5)
  }

  @Test
  internal fun `wins all draws`() {
    assertThat(kingdom.winsAllDraws).isFalse()
    assertThat(kingdom.add(DAS_KOENIGSNEST).winsAllDraws).isTrue()
  }

  @Test
  internal fun compare() {
    val soft = SoftAssertions()
    soft.assertThat(kingdom.compare(0,1)).isFalse()
    soft.assertThat(kingdom.compare(1,1)).isFalse()
    soft.assertThat(kingdom.compare(1,0)).isTrue()
    kingdom = kingdom.add(DAS_KOENIGSNEST)

    soft.assertThat(kingdom.compare(0,1)).isFalse()
    soft.assertThat(kingdom.compare(1,1)).isTrue()
    soft.assertThat(kingdom.compare(1,0)).isTrue()

    soft.assertAll()
  }
}