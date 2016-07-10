package codecomputer

import org.junit.Test
import kotlin.test.assertEquals

class SelectorTest {
    @Test fun selectorTest() {
        ternaryTestHelper(::Selector, false, false, true, true, false, true, false, true, "selector")
    }

    @Test fun multiSelectorBinaryTest() {
        ternaryTestHelper({ a, b, c -> MultiSelector(listOf(a), listOf(b, c)) },
                false, false, true, true, false, true, false, true, "multiselector binary")
    }

    @Test fun multiSelectorTest() {
        val select = 0.toSignals(3)
        val data = 0.toSignals(8)
        val selector = MultiSelector(select, data)
        for (i in 0..255) {
            data.write(i)
            for (j in 0..7) {
                select.write(j)
                selector.readAndAssert(i.toBits(8)[j], "multiselector data $i select $j")
            }
        }
    }

    @Test fun multiDecoderTest() {
        val select = 0.toSignals(3)
        val data = Relay()
        val decoder = MultiDecoder(select, data)
        assertEquals(8, decoder.size, "multidecoder size")
        data.write(true)
        for (i in 0..7) {
            select.write(i)
            decoder.readAndAssert(1 shl i, "multidecoder write true select $i")
        }
        data.write(false)
        for (i in 0..7) {
            select.write(i)
            decoder.readAndAssert(0, "multidecoder write true select $i")
        }
    }
}
