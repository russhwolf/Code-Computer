package codecomputer

import org.junit.Test

class RamTest {

    @Test fun ramTest() {
        val address = 0.toSignals(3)
        val data = Relay()
        val write = Relay()
        val ram = Ram(address, data, write)
        ram.readAndAssert(false, "ram 0")

        data.write(true)
        ram.readAndAssert(false, "ram 1")
        write.write(true)
        write.write(false)
        ram.readAndAssert(true, "ram 2")
        data.write(false)
        ram.readAndAssert(true, "ram 3")

        address.write(5)
        ram.readAndAssert(false, "ram 4")
        data.write(true)
        write.write(true)
        write.write(false)
        data.write(false)
        ram.readAndAssert(true, "ram 5")
        write.write(true)
        write.write(false)
        ram.readAndAssert(false, "ram 6")

        address.write(0)
        ram.readAndAssert(true, "ram 7")
    }

    @Test fun multiRamTest() {
        val address = 0.toSignals(8)
        val data = 0.toSignals(8)
        val write = Relay()
        val ram = MultiRam(address, data, write)

        address.write(0x7e)
        data.write(0xff)
        ram.readAndAssert(0, "multiram 1")
        write.write(true)
        write.write(false)
        ram.readAndAssert(0xff, "multiram 2")
        address.write(0x57)
        ram.readAndAssert(0, "multiram 3")
        address.write(0x7e)
        ram.readAndAssert(0xff, "multiram 4")
    }

    @Test fun controlPanelRamTest() {
        val address = 0.toSignals(8)
        val data = 0.toSignals(8)
        val write = Relay()
        val takeover = Relay()
        val addressOverride = 0.toSignals(8)
        val dataOverride = 0.toSignals(8)
        val writeOverride = Relay()
        val ram = ControlPanelRam(address, data, write, takeover, addressOverride, dataOverride, writeOverride)

        address.write(0xde)
        data.write(0xff)
        ram.readAndAssert(0, "multiram 1")
        write.write(true)
        write.write(false)
        ram.readAndAssert(0xff, "multiram 2")
        address.write(0xad)
        ram.readAndAssert(0, "multiram 3")
        address.write(0xde)
        ram.readAndAssert(0xff, "multiram 4")

        takeover.write(true)

        addressOverride.write(0xbe)
        dataOverride.write(0xff)
        ram.readAndAssert(0, "multiram 1")
        writeOverride.write(true)
        writeOverride.write(false)
        ram.readAndAssert(0xff, "multiram 2")
        addressOverride.write(0xef)
        ram.readAndAssert(0, "multiram 3")
        addressOverride.write(0xbe)
        ram.readAndAssert(0xff, "multiram 4")
    }
}
