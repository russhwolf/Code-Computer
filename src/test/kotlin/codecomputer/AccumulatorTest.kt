package codecomputer

import org.junit.Test

class AccumulatorTest {
    private val A = 27
    private val B = 16
    private val C = 62

    @Test fun accumulatorTest() {
        val input = 0.toSignals(8)
        val add = Relay()
        val clear = Relay()
        val accumulator = Accumulator(input, add, clear)
        accumulator.readAndAssert(0, "accumulator 1")
        input.write(A)
        accumulator.readAndAssert(0, "accumulator 2")
        add.write(true)
        add.write(false)
        accumulator.readAndAssert(A, "accumulator 3")
        input.write(B)
        accumulator.readAndAssert(A, "accumulator 4")
        add.write(true)
        add.write(false)
        accumulator.readAndAssert(A + B, "accumulator 5")
        input.write(C)
        add.write(true)
        add.write(false)
        accumulator.readAndAssert(A + B + C, "accumulator 6")
        add.write(true)
        add.write(false)
        accumulator.readAndAssert(A + B + C + C, "accumulator 7")
        clear.write(true)
        clear.write(false)
        accumulator.readAndAssert(0, "accumulator 8")
    }

    @Test fun ramAccumulatorTest() {
        val oscillator = Oscillator()
        val address = 0.toSignals(4)
        val data = 0.toSignals(8)
        val write = Relay()
        val clear = Relay()
        val takeover = Relay()
        val accumulator = RamAccumulator(oscillator, address, data, write, clear, takeover)
        accumulator.readAndAssert(0, "ram accumulator 1")
        takeover.write(true)
        address.write(0)
        data.write(A)
        write.write(true)
        write.write(false)
        address.write(1)
        data.write(B)
        write.write(true)
        write.write(false)
        address.write(2)
        data.write(C)
        write.write(true)
        write.write(false)
        takeover.write(false)
        accumulator.readAndAssert(0, "ram accumulator 2")
        oscillator.run(2)
        accumulator.readAndAssert(A, "ram accumulator 3")
        oscillator.run(2)
        accumulator.readAndAssert(A + B, "ram accumulator 4")
        oscillator.run(2)
        accumulator.readAndAssert(A + B + C, "ram accumulator 5")
        clear.write(true)
        clear.write(false)
        accumulator.readAndAssert(0, "ram accumulator 6")
    }
}
