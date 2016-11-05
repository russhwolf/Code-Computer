package codecomputer

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CounterTest {

    @Test fun frequencyDividerTest() {
        val clock = Relay()
        val clear = Relay()
        val preset = Relay()
        val counter = FrequencyDivider(clock, clear, preset)
        val signals = listOf(!clock, counter)

        for (i in 0..20) {
            clock.write(!clock.read())
            signals.readAndAssert(i % 4, "ripple $i")
        }
        clear.write(true)
        clear.write(false)
        signals.readAndAssert(0, "counter clear")
        preset.write(true)
        preset.write(false)
        signals.readAndAssert(2, "counter set")
    }

    @Test fun rippleCounterTest() {
        val clock = Relay()
        val clear = Relay()
        val size = 4
        val counter = RippleCounter(size, clock, clear)

        assertEquals(size, counter.size, "counter size")
        for (i in 0..20) {
            counter.readAndAssert(i % 16, "ripple $i")
            clock.write(!clock.read())
            clock.write(!clock.read())
        }
        clear.write(true)
        clear.write(false)
        counter.readAndAssert(0, "counter clear")
    }

    @Test fun addressCounterTest() {
        val clock = Relay()
        val address = 0.toSignals(4)
        val set = Relay()
        val reset = Relay()
        val counter = AddressCounter(clock, address, set, reset)

        counter.readAndAssert(0, "counter 1")
        clock.write(true)
        clock.write(false)
        counter.readAndAssert(1, "counter 2")
        address.write(3)
        counter.readAndAssert(1, "counter 3")
        set.write(true)
        set.write(false)
        counter.readAndAssert(3, "counter 4")
        clock.write(true)
        clock.write(false)
        counter.readAndAssert(4, "counter 5")
        reset.write(true)
        reset.write(false)
        counter.readAndAssert(0, "counter 6")
    }

    @Test fun ringCounterTest() {
        val clock = Relay()
        val clear = Relay()
        val size = 4
        val counter = RingCounter(size, clock, clear)

        val check = (0 until size).map { i ->
            ((i + 1 until size).map { j ->
                counter[i] and counter[j]
            })
        }.flatten().let(::MultiOr)
        check.subscribe { assertFalse(it, "ring counter pairing checker") }

        assertEquals(size, counter.size, "counter size")
        counter.readAndAssert(0, "counter init")
        clear.write(true)
        clear.write(false)
        for ((j, readable) in counter.withIndex()) {
            readable.readAndAssert(readable == counter.last(), "counter[$j] reset")
        }
        for (i in 0..10) {
            clock.write(true)
            for ((j, readable) in counter.withIndex()) {
                readable.readAndAssert(i % size == j, "counter[$j] $i")
            }
            clock.write(false)
            for ((j, readable) in counter.withIndex()) {
                readable.readAndAssert(i % size == j, "counter[$j] $i")
            }
        }
        clear.write(true)
        clock.write(true)
        clear.write(false)
        for ((j, readable) in counter.withIndex()) {
            readable.readAndAssert(readable == counter.last(), "counter[$j] clear")
        }
    }
}
