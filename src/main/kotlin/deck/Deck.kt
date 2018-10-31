package com.github.jangalinski.tidesoftime.deck

import java.util.*

data class Deck(
    private val cards: Queue<Card>
) {
  constructor(shuffle: Boolean = true) : this(Card.ordered(), shuffle)
  constructor(list: List<Card>, shuffle: Boolean = true) : this(
      ArrayDeque(if (shuffle) list.shuffled() else list)
  )

  fun remaining() = cards.size
  fun draw() = cards.remove()!!
}
