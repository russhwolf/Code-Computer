package codecomputer

/**
 * A `ProgrammableAdder` holds two RAM arrays: one holding data and the other holding instructions. The class exposes inputs
 * into these arrays so that they can be programmed. Valid code instructions are [LOD], [STO], [ADD], [SUB],
 * [ADC], [SBB], and [HLT].
 *
 * This class represents the configuration shown on page 215 of *Code*, with some details filled in based on the online
 * technical addendum.
 */
class ProgrammableAdder {
    companion object {
        val ADDRESS_SIZE = 4
        val DATA_SIZE = 8

        val LOD = 0x10
        val STO = 0x11
        val ADD = 0x20
        val SUB = 0x21
        val ADC = 0x22
        val SBB = 0x23
        val HLT = 0xff

        fun List<Readable>.processOpCode(opCode: Int): Readable {
            val bits = opCode.toBits(size)
            return MultiAnd(indices.map { if (bits[it]) this[it] else !this[it] })
        }
    }

    val codeRam: List<Readable>
    val codeRamTakeover: Signal = Relay()
    val codeRamAddressOverride: List<Signal> = 0.toSignals(ADDRESS_SIZE)
    val codeRamDataOverride: List<Signal> = 0.toSignals(DATA_SIZE)
    val codeRamWriteOverride: Signal = Relay()

    val dataRam: List<Readable>
    val dataRamTakeover: Signal = Relay()
    val dataRamAddressOverride: List<Signal> = 0.toSignals(ADDRESS_SIZE)
    val dataRamDataOverride: List<Signal> = 0.toSignals(DATA_SIZE)
    val dataRamWriteOverride: Signal = Relay()

    val reset = Relay()

    private val clock = Oscillator()
    private val halt: Readable

    init {
        val write = Relay()
        val data = 0.toSignals(DATA_SIZE)

        val counter = RippleCounter(ADDRESS_SIZE, clock, reset)

        codeRam = ControlPanelRam(counter, data.map { Constant.FALSE }, Constant.FALSE,
                codeRamTakeover, codeRamAddressOverride, codeRamDataOverride, codeRamWriteOverride)

        dataRam = ControlPanelRam(counter, data, write,
                dataRamTakeover, dataRamAddressOverride, dataRamDataOverride, dataRamWriteOverride)

        val load = codeRam.processOpCode(LOD)
        val store = codeRam.processOpCode(STO)
        val add = codeRam.processOpCode(ADD)
        val subtract = codeRam.processOpCode(SUB)
        val addWithCarry = codeRam.processOpCode(ADC)
        val subtractWithBorrow = codeRam.processOpCode(SBB)
        halt = codeRam.processOpCode(HLT)

        write.link(store and clock)

        val carryIn = Relay()
        val inverter = OnesCompliment(subtract or subtractWithBorrow, dataRam)
        val adder = MultiAdder(carryIn, inverter, data)

        val carryClock = clock and (add or subtract or addWithCarry or subtractWithBorrow)
        val carryLatch = EdgeLatch(carryClock, adder.carry, reset)
        carryIn.link((carryLatch and (addWithCarry or subtractWithBorrow)) or subtract)

        val selector: List<Readable> = dataRam.indices.map { Selector(!load, dataRam[it], adder[it]) }
        val latch: List<Readable> = MultiEdgeLatch(clock, selector, reset)
        data.link(latch)
    }

    fun run() {
        clock.runUntil { halt.read() }
    }
}

fun ProgrammableAdder.writeRam(address: Int, vararg values: Int) {
    codeRamTakeover.write(true)
    dataRamTakeover.write(true)
    for ((i, value) in values.withIndex().groupBy { it.index / 2 }) {
        val code = value[0].value
        val data = if (value.size > 1) value[1].value else 0

        codeRamAddressOverride.write(address + i)
        codeRamDataOverride.write(code)
        codeRamWriteOverride.write(true)
        codeRamWriteOverride.write(false)
        dataRamAddressOverride.write(address + i)
        dataRamDataOverride.write(data)
        dataRamWriteOverride.write(true)
        dataRamWriteOverride.write(false)
    }
    codeRamTakeover.write(false)
    dataRamTakeover.write(false)
}
