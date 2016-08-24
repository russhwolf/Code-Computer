package codecomputer

import codecomputer.ProgrammableAdder.Companion.ADC
import codecomputer.ProgrammableAdder.Companion.ADD
import codecomputer.ProgrammableAdder.Companion.HLT
import codecomputer.ProgrammableAdder.Companion.LOD
import codecomputer.ProgrammableAdder.Companion.SBB
import codecomputer.ProgrammableAdder.Companion.STO
import codecomputer.ProgrammableAdder.Companion.SUB
import org.junit.Test

class ProgrammableAdderTest {
    val programmableAdder = ProgrammableAdder()

    @Test fun processOpCodeTest() {
        with(ProgrammableAdder) {
            val code = 0.toSignals(DATA_SIZE)
            val op = code.processOpCode(0xd0)
            for (i in 0..0xff) {
                code.write(i)
                op.readAndAssert(i == 0xd0, "op code $i")
            }
        }
    }

    @Test fun writeRamTest() {
        with(programmableAdder) {
            codeRam.readAndAssert(0, "write code ram pre")
            dataRam.readAndAssert(0, "write data ram pre")
            writeRam(0x1,
                    0x7e, 0x57, 0xed)
            writeRam(0x8,
                    0xc0, 0xde)
            codeRam.readAndAssert(0, "write code ram pre")
            dataRam.readAndAssert(0, "write data ram pre")

            codeRamTakeover.write(true)
            for (i in 0 until (1 shl ProgrammableAdder.ADDRESS_SIZE)) {
                codeRamAddressOverride.write(i)
                when (i) {
                    0x1 -> codeRam.readAndAssert(0x7e, "write code ram $i")
                    0x2 -> codeRam.readAndAssert(0xed, "write code ram $i")
                    0x8 -> codeRam.readAndAssert(0xc0, "write code ram $i")
                    else -> codeRam.readAndAssert(0x00, "write code ram $i")
                }
            }

            dataRamTakeover.write(true)
            for (i in 0 until (1 shl ProgrammableAdder.ADDRESS_SIZE)) {
                dataRamAddressOverride.write(i)
                when (i) {
                    0x1 -> dataRam.readAndAssert(0x57, "write data ram $i")
                    0x8 -> dataRam.readAndAssert(0xde, "write data ram $i")
                    else -> dataRam.readAndAssert(0x00, "write data ram $i")
                }
            }
        }
    }

    @Test fun programmableAdderLoadStoreTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0x56,
                    STO, 0x10,
                    LOD, 0xf0,
                    STO, 0x15,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0x56, "programmable adder load-store 0")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0x56, "programmable adder load-store 1")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert(0xf0, "programmable adder load-store 2")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0xf0, "programmable adder load-store 3")
        }
    }

    @Test fun programmableAdderAddSubtractTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0x56,
                    ADD, 0x2a,
                    SUB, 0x38,
                    STO, 0x00,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0x56, "programmable adder add-subtract")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0x2a, "programmable adder add-subtract")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert(0x38, "programmable adder add-subtract")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0x56 + 0x2a - 0x38, "programmable adder add-subtract")
        }
    }

    @Test fun programmableAdderAddNoCarryTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0xab,
                    ADD, 0x2c,
                    STO, 0x00,
                    LOD, 0x76,
                    ADC, 0x23,
                    STO, 0x00,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0xab, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0x2c, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert((0xab + 0x2c) and 0xff, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0x76, "programmable adder add-with-carry upper")
            dataRamAddressOverride.write(4)
            dataRam.readAndAssert(0x23, "programmable adder add-with-carry upper")
            dataRamAddressOverride.write(5)
            dataRam.readAndAssert(((0x76 + 0x23 + ((0xab + 0x2c) shr 8)) and 0xff), "programmable adder add-with-carry upper")
        }
    }

    @Test fun programmableAdderAddCarryTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0xff,
                    ADD, 0xff,
                    STO, 0x00,
                    LOD, 0x01,
                    ADC, 0x01,
                    STO, 0x00,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0xff, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0xff, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert((0xff + 0xff) and 0xff, "programmable adder add-with-carry lower")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0x01, "programmable adder add-with-carry upper")
            dataRamAddressOverride.write(4)
            dataRam.readAndAssert(0x01, "programmable adder add-with-carry upper")
            dataRamAddressOverride.write(5)
            dataRam.readAndAssert(((0x01 + 0x01 + ((0xff + 0xff) shr 8)) and 0xff), "programmable adder add-with-carry upper")
        }
    }

    @Test fun programmableAdderSubtractNoBorrowTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0xff,
                    SUB, 0x7f,
                    STO, 0x00,
                    LOD, 0xff,
                    SBB, 0x7f,
                    STO, 0x00,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0xff, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0x7f, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert((0xff - 0x7f) and 0xff, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0xff, "programmable adder subtract-with-borrow upper")
            dataRamAddressOverride.write(4)
            dataRam.readAndAssert(0x7f, "programmable adder subtract-with-borrow upper")
            dataRamAddressOverride.write(5)
            dataRam.readAndAssert(((0xff - 0x7f + ((0xff - 0x7f) shr 8)) and 0xff), "programmable adder subtract-with-borrow upper")
        }
    }

    @Test fun programmableAdderSubtractBorrowTest() {
        with(programmableAdder) {
            writeRam(0x0,
                    LOD, 0x00,
                    SUB, 0xff,
                    STO, 0x00,
                    LOD, 0xff,
                    SBB, 0x00,
                    STO, 0x00,
                    HLT)

            run()

            dataRamTakeover.write(true)
            dataRamAddressOverride.write(0)
            dataRam.readAndAssert(0x00, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(1)
            dataRam.readAndAssert(0xff, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(2)
            dataRam.readAndAssert((0x00 - 0xff) and 0xff, "programmable adder subtract-with-borrow lower")
            dataRamAddressOverride.write(3)
            dataRam.readAndAssert(0xff, "programmable adder subtract-with-borrow upper")
            dataRamAddressOverride.write(4)
            dataRam.readAndAssert(0x00, "programmable adder subtract-with-borrow upper")
            dataRamAddressOverride.write(5)
            dataRam.readAndAssert(((0xff - 0x00 + ((0x00 - 0xff) shr 8)) and 0xff), "programmable adder subtract-with-borrow upper")
        }
    }
}
