package codecomputerdemo

import codecomputer.Computer
import codecomputer.Computer.Companion.ADC
import codecomputer.Computer.Companion.ADD
import codecomputer.Computer.Companion.HLT
import codecomputer.Computer.Companion.JIZ
import codecomputer.Computer.Companion.JNZ
import codecomputer.Computer.Companion.LOD
import codecomputer.Computer.Companion.STO
import codecomputer.read
import codecomputer.write
import codecomputer.writeRam

// Addresses
val input1 = 0x80
val input2 = 0x81
val outputUpper = 0x82
val outputLower = 0x83
val temp = 0x84

val computer = Computer()

fun main(args: Array<String>) {
    with(computer) {
        val input1: Int
        val input2: Int
        if (args.size >= 2) {
            input1 = args[0].toInt()
            input2 = args[1].toInt()
        } else {
            println("Input first factor: ")
            input1 = readLine()?.toInt() ?: 0
            println("Input second factor: ")
            input2 = readLine()?.toInt() ?: 0
        }

        writeMultiplicationProgram(input1, input2)
        println("computing...")
        run()

        val output = (readValue(outputUpper) shl 8) + readValue(outputLower)
        println("$input1 * $input2 = $output")
    }
}

fun Computer.writeMultiplicationProgram(inputValue1: Int, inputValue2: Int) {
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
}

fun Computer.readValue(address: Int): Int {
    ramAddressOverride.write(address)
    ramTakeover.write(true)
    val out = ram.read()
    ramTakeover.write(false)
    return out
}