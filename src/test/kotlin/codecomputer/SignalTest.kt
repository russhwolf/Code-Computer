package codecomputer

import org.junit.Test
import kotlin.test.assertEquals

class SignalTest {
    private val VALUE = 9
    private val BITS = listOf(true, false, false, true)
    private val SIZE = BITS.size
    private val RANGE = 0 until SIZE

    @Test fun relayTest() {
        unaryTestHelper({ it }, false, true, "relay")
    }

    @Test fun subscribeLinkOrderTest() {
        var output = ""
        val a = Relay()
        val b = Relay()
        val c = Relay()
        val d = Relay()
        val e = Relay()

        d.link(b)
        b.link(a)
        c.link(a)
        e.link(c)

        e.subscribe { if (it) output += "e" }
        d.subscribe { if (it) output += "d" }
        c.subscribe { if (it) output += "c" }
        b.subscribe { if (it) output += "b" }
        a.subscribe { if (it) output += "a" }

        a.write(true)

        assertEquals("abcde", output)
    }

    @Test fun intToBitsTest() {
        val bits = VALUE.toBits(SIZE)

        assertEquals(SIZE, bits.size, "Int#toBits() size")
        for (i in RANGE) {
            assertEquals(BITS[i], bits[i], "Int#toBits() $i")
        }
    }

    @Test(expected = IllegalArgumentException::class) fun intToBitsOverflowTest() {
        1000.toBits(3)
    }

    @Test fun intToSignalsTest() {
        val signals = VALUE.toSignals(SIZE)

        assertEquals(SIZE, signals.size, "Int#toSignals() size")
        for (i in RANGE) {
            assertEquals(BITS[i], signals[i].read(), "Int#toSignals() $i")
        }
    }

    @Test fun bitsToIntTest() {
        val value = BITS.toInt()

        assertEquals(VALUE, value, "List<Boolean>#toInt()")
    }

    @Test fun bitsToSignalTest() {
        val signals = BITS.toSignals()

        for (i in RANGE) {
            assertEquals(BITS[i], signals[i].read(), "List<Boolean>#toSignals() $i")
        }
    }

    @Test fun bitsToReadableTest() {
        val readables = BITS.toReadables()

        for (i in RANGE) {
            assertEquals(BITS[i], readables[i].read(), "List<Boolean>#toReadables() $i")
        }
    }

    @Test fun readableToBitsTest() {
        val bits = BITS.toReadables().toBits()

        for (i in RANGE) {
            assertEquals(BITS[i], bits[i], "List<Readable>#toBits() $i")
        }
    }

    @Test fun readIntTest() {
        val value = BITS.toReadables().read()

        assertEquals(VALUE, value, "read int")
    }

    @Test fun readIndexTest() {
        val readables = BITS.toReadables()

        for (i in RANGE) {
            assertEquals(BITS[i], readables.read(i), "read index $i")
        }
    }

    @Test fun writeIndexTest() {
        val signals = BITS.toSignals()

        for (i in RANGE) {
            signals.write(i, !signals[i].read())
        }

        for (i in RANGE) {
            assertEquals(!BITS[i], signals[i].read(), "write index $i")
        }
    }

    @Test fun writeListTest() {
        val signals = BITS.toSignals()

        signals.write(BITS.map { !it })

        for (i in RANGE) {
            assertEquals(!BITS[i], signals[i].read(), "write list $i")
        }
    }

    @Test fun writeIntTest() {
        val signals = BITS.toSignals()

        signals.write(2)

        for (i in RANGE) {
            assertEquals(i == 1, signals[i].read(), "write int $i")
        }
    }

    @Test fun subscribeTest() {
        val readables = BITS.toReadables()
        val verified = readables.indices.map { false }.toMutableList()

        readables.subscribe { i, b -> verified[i] = b }

        for (i in RANGE) {
            assertEquals(BITS[i], verified[i], "subscribe $i")
        }
    }

    @Test fun logTest() {
        Relay.resetLogCount()

        var message = ""
        Relay.logger = { message += "$it\n" }

        val signal = Relay()
        signal.log("signal")
        signal.write(!signal.read())

        val signals = 0.toSignals(2)
        signals.log("signals")
        signals[1].write(true)

        assertEquals(
                "0: signal=false\n" +
                        "1: signal=true\n" +
                        "2: signals[0]=false (0)\n" +
                        "3: signals[1]=false (0)\n" +
                        "4: signals[1]=true (2)\n"
                , message, "log")
    }

    @Test fun monitorTest() {
        val readables = BITS.toReadables()
        val signals = BITS.indices.map { false }.toSignals()

        signals.link(readables) { i, b -> !b }

        for (i in RANGE) {
            assertEquals(!BITS[i], signals[i].read(), "link $i")
        }
    }
}
