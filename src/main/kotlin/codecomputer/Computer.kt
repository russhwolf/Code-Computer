package codecomputer

/**
 * A `Computer` wraps a single [ram][ControlPanelRam] array in the virtual circuitry to make it fully programmable.
 *
 * It should go without saying, because this is simulating everything down to the individual relays, that this is not a
 * fast machine. It will of course vary by development environment, but the clock speed tends to be in the sub-khz range
 *
 * This class represents the culmination of the configuration described in chapter seventeen of *Code*, with many details filled
 * in via the online technical addendum. Notably, the address size is set to 8 instead of 16 bits, allowing for the removal
 * of the upper-digit address latch and reducing the program cycle to three cycles of the clock oscillator instead of four.
 */
class Computer {
    companion object {
        val ADDRESS_SIZE = 8
        val DATA_SIZE = 8

        val LOD = 0x10
        val STO = 0x11
        val ADD = 0x20
        val SUB = 0x21
        val ADC = 0x22
        val SBB = 0x23
        val JMP = 0x30
        val JIZ = 0x31
        val JIC = 0x32
        val JNZ = 0x33
        val JNC = 0x34
        val HLT = 0xff

        fun List<Readable>.processOpCode(opCode: Int): Readable {
            val bits = opCode.toBits(size)
            return MultiAnd(indices.map { if (bits[it]) this[it] else !this[it] })
        }
    }

    val ram: List<Readable>
    val ramTakeover: Signal = Relay()
    val ramAddressOverride: List<Signal> = 0.toSignals(ADDRESS_SIZE)
    val ramDataOverride: List<Signal> = 0.toSignals(DATA_SIZE)
    val ramWriteOverride: Signal = Relay()

    val reset: Signal = Relay()

    internal val clock = Oscillator()
    internal val halt: Readable

    internal val code: List<Readable>
    internal val address: List<Readable>
    internal val data: List<Readable>

    init {
        val code = 0.toSignals(DATA_SIZE)
        val address = 0.toSignals(ADDRESS_SIZE)
        val data = 0.toSignals(DATA_SIZE)
        this.code = code
        this.address = address
        this.data = data

        val write = Relay()
        val set = Relay()

        val (codeClock, addressClock, dataClock) = RingCounter(3, clock, reset)

        val counterClock = LevelLatch(codeClock or dataClock, clock)

        val counter = AddressCounter(counterClock, address, set, reset)
        val addressSelector = address.indices.map { Selector(!addressClock, address[it], counter[it]) }

        ram = ControlPanelRam(addressSelector, data, write,
                ramTakeover, ramAddressOverride, ramDataOverride, ramWriteOverride)

        val codeLatch = MultiEdgeLatch(codeClock, ram, reset)
        code.link(codeLatch)
        val addressLatch = MultiEdgeLatch(addressClock, ram, reset)
        address.link(addressLatch)

        val load = code.processOpCode(LOD)
        val store = code.processOpCode(STO)
        val add = code.processOpCode(ADD)
        val subtract = code.processOpCode(SUB)
        val addWithCarry = code.processOpCode(ADC)
        val subtractWithBorrow = code.processOpCode(SBB)
        val jump = code.processOpCode(JMP)
        val jumpIfZero = code.processOpCode(JIZ)
        val jumpIfCarry = code.processOpCode(JIC)
        val jumpIfNotZero = code.processOpCode(JNZ)
        val jumpIfNotCarry = code.processOpCode(JNC)
        halt = code.processOpCode(HLT)

        write.link(store and addressClock and !clock)

        val carryIn = Relay()
        val inverter = OnesCompliment(subtract or subtractWithBorrow, ram)
        val adder = MultiAdder(carryIn, inverter, data)

        val carryClock = dataClock and MultiOr(add, subtract, addWithCarry, subtractWithBorrow)
        val carryLatch = EdgeLatch(carryClock, adder.carry, reset)
        carryIn.link((carryLatch and (addWithCarry or subtractWithBorrow)) or subtract)

        val dataLatchClock = dataClock and MultiOr(add, subtract, addWithCarry, subtractWithBorrow, load)
        val dataSelector: List<Readable> = ram.indices.map { Selector(!load, ram[it], adder[it]) }
        val dataLatch: List<Readable> = MultiEdgeLatch(dataLatchClock, dataSelector, reset)
        data.link(dataLatch)

        val zeroLatch = EdgeLatch(dataLatchClock, MultiNor(adder), reset)
        set.link(MultiOr(
                jump,
                jumpIfZero and zeroLatch,
                jumpIfCarry and carryLatch,
                jumpIfNotZero and zeroLatch.bar,
                jumpIfNotCarry and carryLatch.bar
        ) and dataClock)
    }

    fun run() {
        reset.write(true)
        reset.write(false)
        clock.runUntil { halt.read() }
    }
}

/**
 * Write values to the RAM of this computer. The `values` inputs will be written in consecutive addresses starting with
 * `address`.
 */
fun Computer.writeRam(address: Int, vararg values: Int) {
    ramTakeover.write(true)
    for ((i, value) in values.withIndex()) {
        ramAddressOverride.write(address + i)
        ramDataOverride.write(value)
        ramWriteOverride.write(true)
        ramWriteOverride.write(false)
    }
    ramTakeover.write(false)
}
