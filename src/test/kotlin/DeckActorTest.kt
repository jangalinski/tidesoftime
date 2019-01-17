package com.github.jangalinski.tidesoftime

import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

@InternalCoroutinesApi
class TestDirectContext : CoroutineDispatcher(), Delay {
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        continuation.resume(Unit)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class DeckActorTest {
    @Test
    internal fun `initialize deck`() {
        // GIVEN
        val cards = Card.values().toList()
        val deck = createDeck(cards)

        // WHEN
        val (remainingCards, size) = runBlocking(TestDirectContext()) {
            deck.remainingCards() to deck.size()
        }

        // THEN
        assertThat(remainingCards).containsAll(cards)
        assertThat(remainingCards).hasSize(size)
        assertThat(remainingCards).hasSize(cards.size)
    }

    @Test
    internal fun `take cards from the deck`() {
        val cards = Card.values().toList()

        val deck = createDeck(cards)

        val (card, remainingCards, size) = runBlocking(TestDirectContext()) {
            Triple(deck.deal(), deck.remainingCards(), deck.size())
        }

        assertThat(card).isIn(cards)
        assertThat(remainingCards).isSubsetOf(cards)
        assertThat(cards - remainingCards).isEqualTo(listOf(card))
        assertThat(remainingCards).hasSize(size)
        assertThat(remainingCards).hasSize(cards.size - 1)
    }
}

