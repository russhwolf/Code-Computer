package codecomputer

import kotlin.test.assertEquals

fun Readable.readAndAssert(value: Boolean, label: String? = null) = assertEquals(value, read(), label)

fun List<Readable>.readAndAssert(value: Int, label: String? = null) {
    assertEquals(value, read(), label)
}

inline fun unaryTestHelper(operator: (Readable) -> Readable, expF: Boolean, expT: Boolean, label: String) {
    val input = Relay()
    val output = operator(input)
    input.write(false)
    output.readAndAssert(expF, "$label F")
    input.write(true)
    output.readAndAssert(expT, "$label T")
}

inline fun binaryTestHelper(operator: (Readable, Readable) -> Readable,
                            expFF: Boolean, expFT: Boolean, expTF: Boolean, expTT: Boolean,
                            label: String) {
    val input1 = Relay()
    val input2 = Relay()
    val output = operator(input1, input2)
    input1.write(false)
    input2.write(false)
    output.readAndAssert(expFF, "$label FF")
    input1.write(false)
    input2.write(true)
    output.readAndAssert(expFT, "$label FT")
    input1.write(true)
    input2.write(false)
    output.readAndAssert(expTF, "$label TF")
    input1.write(true)
    input2.write(true)
    output.readAndAssert(expTT, "$label TT")
}

inline fun ternaryTestHelper(operator: (Readable, Readable, Readable) -> Readable,
                             expFFF: Boolean, expFFT: Boolean, expFTF: Boolean, expFTT: Boolean,
                             expTFF: Boolean, expTFT: Boolean, expTTF: Boolean, expTTT: Boolean,
                             label: String) {
    val input1 = Relay()
    val input2 = Relay()
    val input3 = Relay()
    val output = operator(input1, input2, input3)
    input1.write(false)
    input2.write(false)
    input3.write(false)
    output.readAndAssert(expFFF, "$label FFF")
    input1.write(false)
    input2.write(false)
    input3.write(true)
    output.readAndAssert(expFFT, "$label FFT")
    input1.write(false)
    input2.write(true)
    input3.write(false)
    output.readAndAssert(expFTF, "$label FTF")
    input1.write(false)
    input2.write(true)
    input3.write(true)
    output.readAndAssert(expFTT, "$label FTT")
    input1.write(true)
    input2.write(false)
    input3.write(false)
    output.readAndAssert(expTFF, "$label TFF")
    input1.write(true)
    input2.write(false)
    input3.write(true)
    output.readAndAssert(expTFT, "$label TFT")
    input1.write(true)
    input2.write(true)
    input3.write(false)
    output.readAndAssert(expTTF, "$label TTF")
    input1.write(true)
    input2.write(true)
    input3.write(true)
    output.readAndAssert(expTTT, "$label TTT")
}