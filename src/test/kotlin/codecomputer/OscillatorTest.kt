package codecomputer

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class OscillatorTest {
    private val CYCLES = 10

    val oscillator = Oscillator()
    private var counter = 0

    @Before
    fun setup() {
        counter = 0
    }

    @Test fun oscillatorTest() {
        oscillator.run(CYCLES) {
            counter = it
            oscillator.readAndAssert(it % 2 != 0, "oscillator event $it")
        }

        assertEquals(CYCLES - 1, counter, "oscillator event count")
    }

    @Test fun oscillatorTestRun1() {
        for (i in 0 until CYCLES) {
            oscillator.run(1) {
                oscillator.readAndAssert(i % 2 != 0, "oscillator event $i")
                counter = i
            }
        }

        assertEquals(CYCLES - 1, counter, "oscillator event count")
    }
}