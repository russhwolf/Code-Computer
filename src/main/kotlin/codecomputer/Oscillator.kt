package codecomputer

/**
 * An `Oscillator` is a [Readable] which toggles its own state back and forth. It is intended for use as a clocking input.
 * An oscillator should not be run indefinitely, but instead provides methods to run for a specified number of cycles
 * or until a given condition is met.
 *
 * This class is meant to represent the oscillator described on page 157 of *Code*, although the internal implementation
 * is different.
 */
class Oscillator private constructor(private val delegate: Signal) : Readable by delegate {
    private var active: Boolean = false

    constructor(state: Boolean = false) : this(Relay(state))

    /**
     * Run this [Oscillator] until the given condition is `true`
     */
    inline fun runUntil(crossinline condition: () -> Boolean) {
        val subscriber: (Boolean) -> Unit = {
            if (condition()) {
                stop()
            }
        }
        subscribe(subscriber)
        start()
        unsubscribe(subscriber)
    }

    /**
     * Run this [Oscillator] for the given number of cycles. The listener function, if supplied, will be called each time
     * and passed the number of cycles which have occurred.
     */
    fun run(cycles: Int, listener: (Int) -> Unit = {}) {
        var i = 0
        runUntil {
            val out = i >= cycles
            if (!out) {
                listener(i)
                i++
            }
            out
        }
    }

    fun start(): Unit {
        if (active) return
        active = true
        while (active) {
            delegate.write(!read())
        }
    }

    fun stop(): Unit {
        active = false
    }
}
