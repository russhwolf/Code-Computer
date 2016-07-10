package codecomputer

import org.junit.Test

class GateTest {
    @Test fun inverterTest() {
        unaryTestHelper({ !it }, true, false, "inverter")
    }

    @Test fun andTest() {
        binaryTestHelper({ a, b -> a and b }, false, false, false, true, "and")
    }

    @Test fun multiAndTest() {
        ternaryTestHelper({ a, b, c -> MultiAnd(a, b, c) }, false, false, false, false, false, false, false, true, "multiAnd")
    }

    @Test fun orTest() {
        binaryTestHelper({ a, b -> a or b }, false, true, true, true, "or")
    }

    @Test fun multiOrTest() {
        ternaryTestHelper({ a, b, c -> MultiOr(a, b, c) }, false, true, true, true, true, true, true, true, "multiAnd")
    }

    @Test fun nandTest() {
        binaryTestHelper({ a, b -> a nand b }, true, true, true, false, "nand")
    }

    @Test fun multiNandTest() {
        ternaryTestHelper({ a, b, c -> MultiNand(a, b, c) }, true, true, true, true, true, true, true, false, "multiAnd")
    }

    @Test fun norTest() {
        binaryTestHelper({ a, b -> a nor b }, true, false, false, false, "nor")
    }

    @Test fun multiNorTest() {
        ternaryTestHelper({ a, b, c -> MultiNor(a, b, c) }, true, false, false, false, false, false, false, false, "multiAnd")
    }

    @Test fun xorTest() {
        binaryTestHelper({ a, b -> a xor b }, false, true, true, false, "xor")
    }
}
