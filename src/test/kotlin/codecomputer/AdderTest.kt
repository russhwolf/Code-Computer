package codecomputer

import org.junit.Test

class AdderTest {
    private val A = 90
    private val B = 159
    private val C = 235
    private val D = 214

    @Test fun halfAdderTest() {
        binaryTestHelper(::HalfAdder, false, true, true, false, "half adder sum")
        binaryTestHelper({ a, b -> HalfAdder(a, b).carry }, false, false, false, true, "half adder carry")
    }

    @Test fun fullAdderTest() {
        ternaryTestHelper(::FullAdder, false, true, true, false, true, false, false, true, "full adder sum")
        ternaryTestHelper({ a, b, c -> FullAdder(a, b, c).carry }, false, false, false, true, false, true, true, true, "full adder carry")
    }

    @Test fun multiAdderTest() {
        val carry = Relay(false)
        val a = A.toSignals(8)
        val b = B.toSignals(8)

        val adder = MultiAdder(carry, a, b)
        adder.readAndAssert(A + B, "multiadder sum 1")
        adder.carry.readAndAssert(false, "multi adder carry 1")

        carry.write(true)
        adder.readAndAssert(A + B + 1, "multiadder sum 2")
        adder.carry.readAndAssert(false, "multi adder carry 2")

        carry.write(false)
        a.write(C)
        b.write(D)
        adder.readAndAssert((C + D) % 256, "multiadder sum 3")
        adder.carry.readAndAssert(true, "multi adder carry 3")
    }

    @Test fun addSubtractTest() {
        val sub = Relay(false)
        val a = A.toSignals(8)
        val b = B.toSignals(8)

        val addSubtract = AddSubtract(sub, a, b)
        addSubtract.readAndAssert(A + B, "addsubtract output 1")
        addSubtract.overflow.readAndAssert(false, "addsubtract overflow 1")

        sub.write(true)
        addSubtract.readAndAssert(256 + (A - B), "addsubtract output 2")
        addSubtract.overflow.readAndAssert(true, "addsubtract overflow 2")

        a.write(C)
        b.write(D)
        addSubtract.readAndAssert(C - D, "addsubtract output 3")
        addSubtract.overflow.readAndAssert(false, "addsubtract overflow 3")

        sub.write(false)
        addSubtract.readAndAssert((C + D) % 256, "addsubtract output 3")
        addSubtract.overflow.readAndAssert(true, "addsubtract overflow 3")
    }
}

