package com.github.jangalinski.tidesoftime.player

import com.github.jangalinski.tidesoftime.Card
import com.github.jangalinski.tidesoftime.game.Hand
import com.github.jangalinski.tidesoftime.game.Kingdom

interface PlayerStrategy {

  fun playHandCard(hand: Hand) : Card

  fun keepKingdomCard(kingdom: Kingdom) : Card

  fun destroyKingdomCard(kingdom: Kingdom) : Card

}

object RandomCardSelection : PlayerStrategy {
  override fun playHandCard(hand: Hand): Card = hand.shuffled().first()
  override fun keepKingdomCard(kingdom: Kingdom): Card = kingdom.unmarked().shuffled().first().card
  override fun destroyKingdomCard(kingdom: Kingdom): Card = kingdom.unmarked().shuffled().first().card
  override fun toString(): String = this::class.simpleName!!
}

object ConsoleSelectionStrategy : PlayerStrategy {
  override fun playHandCard(hand: Hand): Card {
    val list = hand.toList()

    list.withIndex().forEach{ (i, c) ->
      println("[$i] $c")
    }
    print("-> choose card: ")
    return list[readNumber(list.size)]
  }

  override fun keepKingdomCard(kingdom: Kingdom): Card {
    val list = kingdom.toList()

    list.withIndex().forEach{ (i, c) ->
      println("[$i] $c")
    }

    println("-> choose card: ")
    return list[readNumber(list.size)].card
  }

  override fun destroyKingdomCard(kingdom: Kingdom): Card {
    val list = kingdom.toList()

    list.withIndex().forEach{ (i, c) ->
      println("[$i] $c")
    }
    return list[readNumber(list.size)].card
  }

  private fun readNumber(max: Int) = readLine()!!
      .filter { it.isDigit()}.toInt()

  override fun toString(): String = this::class.simpleName!!
}