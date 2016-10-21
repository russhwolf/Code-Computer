package codecomputer

import codecomputer.Computer.Companion.ADC
import codecomputer.Computer.Companion.ADD
import codecomputer.Computer.Companion.HLT
import codecomputer.Computer.Companion.JIZ
import codecomputer.Computer.Companion.JNZ
import codecomputer.Computer.Companion.LOD
import codecomputer.Computer.Companion.STO
import org.junit.Ignore
import org.junit.Test

class ComputerMultiplyTest {

    // Addresses
    val input1 = 0x80
    val input2 = 0x81
    val outputUpper = 0x82
    val outputLower = 0x83
    val temp = 0x84

    /**
     * This test runs the multiplication routine described on pages 228-230 of *Code*. It has been modified to address the
     * bug described on page 236, so that it can be safely rerun multiple times. It has also been modified to safely multiply
     * by zero, and to take only a single byte for each input rather than two, because the original code does not handle
     * a nonzero upper byte correctly.
     *
     * Runtime is proportional to the value of the second input. Since this can take a while it probably shouldn't run with
     * the rest of the unit tests.
     */
    @Ignore @Test fun multiplyTest() {
        multiplyAndCheck(0x02, 0x02)
        multiplyAndCheck(0x02, 0x00)
        multiplyAndCheck(0x00, 0x02)
        multiplyAndCheck(0x00, 0x00)
        multiplyAndCheck(0xff, 0x02)
        multiplyAndCheck(0x02, 0xff)
        multiplyAndCheck(0xff, 0xff)
    }

    private fun multiplyAndCheck(inputValue1: Int, inputValue2: Int) {
        with(Computer()) {
            writeRam(0x80,
                    inputValue1,
                    inputValue2,
                    0x00, // output
                    0x00, // output overflow
                    0x00) // temp storage - always 0 when program is not running

            writeRam(0x00,
                    // Load value from temp storage (will be 0) into output
                    LOD, temp,
                    STO, outputUpper,
                    STO, outputLower,

                    // Load input 2 into temp storage
                    LOD, input2,
                    JIZ, 0x20, // Halt right now if input 2 is zero
                    STO, temp,

                    // Begin multiplication loop

                    // add input 1 to output lower digit
                    LOD, outputLower,
                    ADD, input1,
                    STO, outputLower,

                    // add carry bit to output upper digit
                    LOD, outputUpper,
                    ADC, 0x21, // Value at this address is always 0
                    STO, outputUpper,

                    // subtract 1 from temp
                    LOD, temp,
                    ADD, 0x20, // Value at this address is HLT == 0xff which adds as -1.
                    STO, temp,

                    // Loop if temp is still nonzero, otherwise halt
                    JNZ, 0x0c,
                    HLT, 0x00)

            // Run/check twice to prove that we fixed the bug
            runAndCheck(inputValue1, inputValue2)
            runAndCheck(inputValue1, inputValue2)
        }
    }

    private fun Computer.runAndCheck(inputValue1: Int, inputValue2: Int) {
        run()

        ramTakeover.write(true)
        ramAddressOverride.write(input1)
        ram.readAndAssert(inputValue1, "computer multiply input 1")
        ramAddressOverride.write(input2)
        ram.readAndAssert(inputValue2, "computer multiply input 2")
        ramAddressOverride.write(outputUpper)
        ram.readAndAssert((inputValue1 * inputValue2) shr 8, "computer multiply output upper")
        ramAddressOverride.write(outputLower)
        ram.readAndAssert((inputValue1 * inputValue2) and 0xff, "computer multiply output lower")
        ramAddressOverride.write(temp)
        ram.readAndAssert(0, "computer multiply temp")
        ramTakeover.write(false)
    }
}