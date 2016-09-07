package codecomputer

import codecomputer.Computer.Companion.ADC
import codecomputer.Computer.Companion.ADD
import codecomputer.Computer.Companion.HLT
import codecomputer.Computer.Companion.JIC
import codecomputer.Computer.Companion.JIZ
import codecomputer.Computer.Companion.JMP
import codecomputer.Computer.Companion.JNC
import codecomputer.Computer.Companion.JNZ
import codecomputer.Computer.Companion.LOD
import codecomputer.Computer.Companion.SBB
import codecomputer.Computer.Companion.STO
import codecomputer.Computer.Companion.SUB
import org.junit.Test

class ComputerTest {
    private val computer = Computer()

    @Test fun processOpCodeTest() {
        with(Computer) {
            val code = 0.toSignals(Computer.DATA_SIZE)
            val op = code.processOpCode(0xd0)
            for (i in 0..0xff) {
                code.write(i)
                op.readAndAssert(i == 0xd0, "op code $i")
            }
        }
    }

    @Test fun writeRamTest() {
        with(computer) {
            ram.readAndAssert(0, "write ram pre")
            writeRam(0x11,
                    0x7e, 0x57, 0xed)
            writeRam(0x22,
                    0xc0, 0xde)
            ram.readAndAssert(0, "write ram pre")

            ramTakeover.write(true)
            for (i in 0 until (1 shl Computer.DATA_SIZE)) {
                ramAddressOverride.write(i)
                when (i) {
                    0x11 -> ram.readAndAssert(0x7e, "write ram $i")
                    0x12 -> ram.readAndAssert(0x57, "write ram $i")
                    0x13 -> ram.readAndAssert(0xed, "write ram $i")
                    0x22 -> ram.readAndAssert(0xc0, "write ram $i")
                    0x23 -> ram.readAndAssert(0xde, "write ram $i")
                    else -> ram.readAndAssert(0x00, "write ram $i")
                }
            }
        }
    }

    @Test fun loadTest() {
        with(computer) {
            writeRam(0x80,
                    0x0a,
                    0xa0)

            writeRam(0x00,
                    LOD, 0x80,
                    LOD, 0x81,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(LOD, "code 1")
            clock.run(2)
            address.readAndAssert(0x80, "address 1")
            clock.run(2)
            data.readAndAssert(0x0a, "data 1")
            clock.run(2)
            code.readAndAssert(LOD, "code 2")
            clock.run(2)
            address.readAndAssert(0x81, "address 2")
            clock.run(2)
            data.readAndAssert(0xa0, "data 2")
            clock.run(2)
            code.readAndAssert(HLT, "code 3")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun jumpTest() {
        with(computer) {
            writeRam(0x80,
                    0x0a,
                    0xa0)

            writeRam(0x00,
                    JMP, 0x10,
                    HLT)
            writeRam(0x10,
                    LOD, 0x81,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(JMP, "code 1")
            clock.run(2)
            address.readAndAssert(0x10, "address 1")
            clock.run(2)
            data.readAndAssert(0x00, "data 1")
            clock.run(2)
            code.readAndAssert(LOD, "code 2")
            clock.run(2)
            address.readAndAssert(0x81, "address 2")
            clock.run(2)
            data.readAndAssert(0xa0, "data 2")
            clock.run(2)
            code.readAndAssert(HLT, "code 3")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun jumpIfNotZeroTest() {
        with(computer) {
            writeRam(0x80,
                    0x0a,
                    0x05)

            writeRam(0x00,
                    LOD, 0x80,
                    SUB, 0x81,
                    JNZ, 0x02,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(LOD, "code 1")
            clock.run(2)
            address.readAndAssert(0x80, "address 1")
            clock.run(2)
            data.readAndAssert(0x0a, "data 1")
            clock.run(2)
            code.readAndAssert(SUB, "code 2")
            clock.run(2)
            address.readAndAssert(0x81, "address 2")
            clock.run(2)
            data.readAndAssert(0x05, "data 2")
            clock.run(2)
            code.readAndAssert(JNZ, "code 3")
            clock.run(2)
            address.readAndAssert(0x02, "address 3")
            clock.run(2)
            data.readAndAssert(0x05, "data 3")
            clock.run(2)
            code.readAndAssert(SUB, "code 4")
            clock.run(2)
            address.readAndAssert(0x81, "address 4")
            clock.run(2)
            data.readAndAssert(0x00, "data 4")
            clock.run(2)
            code.readAndAssert(JNZ, "code 5")
            clock.run(2)
            address.readAndAssert(0x02, "address 5")
            clock.run(2)
            data.readAndAssert(0x00, "data 5")
            clock.run(2)
            code.readAndAssert(HLT, "code 6")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun jumpIfNotCarryTest() {
        with(computer) {
            writeRam(0x80,
                    0xff)

            writeRam(0x00,
                    ADC, 0x80,
                    JNC, 0x00,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(ADC, "code 1")
            clock.run(2)
            address.readAndAssert(0x80, "address 1")
            clock.run(2)
            data.readAndAssert(0xff, "data 1")
            clock.run(2)
            code.readAndAssert(JNC, "code 2")
            clock.run(2)
            address.readAndAssert(0x00, "address 2")
            clock.run(2)
            data.readAndAssert(0xff, "data 2")
            clock.run(2)
            code.readAndAssert(ADC, "code 3")
            clock.run(2)
            address.readAndAssert(0x80, "address 3")
            clock.run(2)
            data.readAndAssert(0xfe, "data 3")
            clock.run(2)
            code.readAndAssert(JNC, "code 4")
            clock.run(2)
            address.readAndAssert(0x00, "address 4")
            clock.run(2)
            data.readAndAssert(0xfe, "data 4")
            clock.run(2)
            code.readAndAssert(HLT, "code 5")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun jumpIfZeroTest() {
        with(computer) {
            writeRam(0x80,
                    0x0a,
                    0x05)

            writeRam(0x00,
                    LOD, 0x80,
                    SUB, 0x81,
                    JIZ, 0x08,
                    JMP, 0x02,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(LOD, "code 1")
            clock.run(2)
            address.readAndAssert(0x80, "address 1")
            clock.run(2)
            data.readAndAssert(0x0a, "data 1")
            clock.run(2)
            code.readAndAssert(SUB, "code 2")
            clock.run(2)
            address.readAndAssert(0x81, "address 2")
            clock.run(2)
            data.readAndAssert(0x05, "data 2")
            clock.run(2)
            code.readAndAssert(JIZ, "code 3")
            clock.run(2)
            address.readAndAssert(0x08, "address 3")
            clock.run(2)
            data.readAndAssert(0x05, "data 3")
            clock.run(2)
            code.readAndAssert(JMP, "code 4")
            clock.run(2)
            address.readAndAssert(0x02, "address 4")
            clock.run(2)
            data.readAndAssert(0x05, "data 4")
            clock.run(2)
            code.readAndAssert(SUB, "code 5")
            clock.run(2)
            address.readAndAssert(0x81, "address 5")
            clock.run(2)
            data.readAndAssert(0x00, "data 5")
            clock.run(2)
            code.readAndAssert(JIZ, "code 6")
            clock.run(2)
            address.readAndAssert(0x08, "address 6")
            clock.run(2)
            data.readAndAssert(0x00, "data 6")
            clock.run(2)
            code.readAndAssert(HLT, "code 7")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun jumpIfCarryTest() {
        with(computer) {
            writeRam(0x80,
                    0xff)

            writeRam(0x00,
                    ADC, 0x80,
                    JIC, 0x06,
                    JMP, 0x00,
                    HLT)

            reset.write(true)
            reset.write(false)

            clock.run(2)
            code.readAndAssert(ADC, "code 1")
            clock.run(2)
            address.readAndAssert(0x80, "address 1")
            clock.run(2)
            data.readAndAssert(0xff, "data 1")
            clock.run(2)
            code.readAndAssert(JIC, "code 2")
            clock.run(2)
            address.readAndAssert(0x06, "address 2")
            clock.run(2)
            data.readAndAssert(0xff, "data 2")
            clock.run(2)
            code.readAndAssert(JMP, "code 3")
            clock.run(2)
            address.readAndAssert(0x00, "address 3")
            clock.run(2)
            data.readAndAssert(0xff, "data 3")
            clock.run(2)
            code.readAndAssert(ADC, "code 4")
            clock.run(2)
            address.readAndAssert(0x80, "address 4")
            clock.run(2)
            data.readAndAssert(0xfe, "data 4")
            clock.run(2)
            code.readAndAssert(JIC, "code 5")
            clock.run(2)
            address.readAndAssert(0x06, "address 5")
            clock.run(2)
            data.readAndAssert(0xfe, "data 5")
            clock.run(2)
            code.readAndAssert(HLT, "code 6")
            halt.readAndAssert(true, "halt")
        }
    }

    @Test fun loadStoreTest() {
        with(computer) {
            writeRam(0x80,
                    0x0a,
                    0x00,
                    0xa0,
                    0x00)

            writeRam(0x00,
                    LOD, 0x80,
                    STO, 0x81,
                    LOD, 0x82,
                    STO, 0x83,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0x0a, "computer load-store 0")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0x0a, "computer load-store 1")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0xa0, "computer load-store 2")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0xa0, "computer load-store 3")
        }
    }

    @Test fun addSubtractTest() {
        with(computer) {
            writeRam(0x80,
                    0x56,
                    0x2a,
                    0x38,
                    0x00)

            writeRam(0x00,
                    LOD, 0x80,
                    ADD, 0x81,
                    SUB, 0x82,
                    STO, 0x83,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0x56, "computer add-subtract")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0x2a, "computer add-subtract")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0x38, "computer add-subtract")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0x56 + 0x2a - 0x38, "computer add-subtract")
        }
    }

    @Test fun computerAddNoCarryTest() {
        with(computer) {
            writeRam(0x80,
                    0x76,
                    0xab,
                    0x23,
                    0x2c)

            writeRam(0x00,
                    LOD, 0x81,
                    ADC, 0x83,
                    STO, 0x85,
                    LOD, 0x80,
                    ADD, 0x82,
                    STO, 0x84,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0x76, "computer add-no-carry")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0xab, "computer add-no-carry")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0x23, "computer add-no-carry")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0x2c, "computer add-no-carry")
            ramAddressOverride.write(0x84)
            ram.readAndAssert(((0x76 + 0x23 + ((0xab + 0x2c) shr 8)) and 0xff), "computer add-no-carry")
            ramAddressOverride.write(0x85)
            ram.readAndAssert((0xab + 0x2c) and 0xff, "computer add-no-carry")
        }
    }

    @Test fun computerAddCarryTest() {
        with(computer) {
            writeRam(0x80,
                    0x01,
                    0xff,
                    0x01,
                    0xff)

            writeRam(0x00,
                    LOD, 0x81,
                    ADD, 0x83,
                    STO, 0x85,
                    LOD, 0x80,
                    ADC, 0x82,
                    STO, 0x84,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0x01, "computer add-with-carry")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0xff, "computer add-with-carry")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0x01, "computer add-with-carry")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0xff, "computer add-with-carry")
            ramAddressOverride.write(0x84)
            ram.readAndAssert(((0x01 + 0x01 + ((0xff + 0xff) shr 8)) and 0xff), "computer add-with-carry")
            ramAddressOverride.write(0x85)
            ram.readAndAssert((0xff + 0xff) and 0xff, "computer add-with-carry")
        }
    }

    @Test fun computerSubtractNoBorrowTest() {
        with(computer) {
            writeRam(0x80,
                    0xff,
                    0xff,
                    0x7f,
                    0x7f)

            writeRam(0x00,
                    LOD, 0x81,
                    SUB, 0x83,
                    STO, 0x85,
                    LOD, 0x80,
                    SBB, 0x82,
                    STO, 0x84,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0xff, "computer subtract-no-borrow")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0xff, "computer subtract-no-borrow")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0x7f, "computer subtract-no-borrow")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0x7f, "computer subtract-no-borrow")
            ramAddressOverride.write(0x84)
            ram.readAndAssert(((0xff - 0x7f + ((0xff - 0x7f) shr 8)) and 0xff), "computer subtract-no-borrow")
            ramAddressOverride.write(0x85)
            ram.readAndAssert((0xff - 0x7f) and 0xff, "computer subtract-no-borrow")
        }
    }

    @Test fun computerSubtractBorrowTest() {
        with(computer) {
            writeRam(0x80,
                    0xff,
                    0x00,
                    0x00,
                    0xff)

            writeRam(0x00,
                    LOD, 0x81,
                    SUB, 0x83,
                    STO, 0x85,
                    LOD, 0x80,
                    SBB, 0x82,
                    STO, 0x84,
                    HLT)

            run()

            ramTakeover.write(true)
            ramAddressOverride.write(0x80)
            ram.readAndAssert(0xff, "computer subtract-with-borrow")
            ramAddressOverride.write(0x81)
            ram.readAndAssert(0x00, "computer subtract-with-borrow")
            ramAddressOverride.write(0x82)
            ram.readAndAssert(0x00, "computer subtract-with-borrow")
            ramAddressOverride.write(0x83)
            ram.readAndAssert(0xff, "computer subtract-with-borrow")
            ramAddressOverride.write(0x84)
            ram.readAndAssert(((0xff - 0x00 + ((0x00 - 0xff) shr 8)) and 0xff), "computer subtract-with-borrow")
            ramAddressOverride.write(0x85)
            ram.readAndAssert((0x00 - 0xff) and 0xff, "computer subtract-with-borrow")
        }
    }
}
