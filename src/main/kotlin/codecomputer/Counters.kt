package codecomputer

/**
 * A `FrequencyDivider` changes its state half as often as the `clock` input, toggling whenever `clock` changes from
 * `false` to `true`.
 *
 * This class represents the frequency divider shown on page 173 of *Code*
 *
 * @see RippleCounter
 * @see AddressCounter
 */
class FrequencyDivider(clock: Readable, clear: Readable = Constant.FALSE, preset: Readable = Constant.FALSE,
                       state: Boolean = true) : RelayBar(state) {
    override val bar: Readable

    init {
        val bar = Relay(!state)
        val latch = EdgeLatch(clock, bar, clear, preset, state)
        delegate.link(latch)
        bar.link(latch.bar)
        this.bar = bar
    }
}

/**
 * A `RippleCounter` increments the integer representation of its output when the `clock` input changes from `false` to
 * `true`, returning to `0` when it overflows.
 *
 * This class represents the Ripple Counter described on page 177 of *Code*
 *
 * @see FrequencyDivider
 */
class RippleCounter(size: Int, clock: Readable, clear: Readable = Constant.FALSE, state: Int = 0) :
        List<Readable> by ((0 until size).fold(mutableListOf<Invertible>()) { list, i ->
            list.apply { add(FrequencyDivider(if (i == 0) !clock else list[i - 1].bar, clear, state = state.nthBit(i))) }
        })

/**
 * An `AddressCounter` acts as a [RippleCounter] which is settable. It increments when the `clock` input changes from
 * `false` to `true`, but also jumps directly to the value at `address` if `set` is true.
 *
 * This class adjusts the [RippleCounter] to incorporate the extra circuitry on page 227 of *Code*
 *
 * @see FrequencyDivider
 */
class AddressCounter(clock: Readable, address: List<Readable>, set: Readable = Constant.FALSE,
                     reset: Readable = Constant.FALSE, state: Int = 0) :
        List<Invertible> by ((0..address.lastIndex).fold(mutableListOf<Invertible>()) { list, i ->
            list.apply {
                add(FrequencyDivider(
                        if (i == 0) !clock else list[i - 1].bar,
                        set and !address[i] or reset,
                        set and address[i],
                        state.nthBit(i)
                ))
            }
        })

/**
 * A `RingCounter` toggles between its outputs such that at most one is true at any given time. At construction time all
 * outputs are false, and they will not change until the `clear` input is set to `true`. At that point, the output in the
 * highest index will be `true` and the others `false`. Once the `clear` input is false again, the outputs will rotate
 * every time the `clock` input changes from `true` to `false`.
 *
 * This class represents the ring counter described in the Page 222 section of the *Code* online technical addendum.
 */
class RingCounter(size: Int, clock: Readable, clear: Readable) :
        List<Readable> by ((1..size).map { Relay() }.apply {
            for ((i, it) in this.withIndex()) {
                when (i) {
                    0 -> it.link(EdgeLatch(clock, last(), clear))
                    lastIndex -> it.link(EdgeLatch(clock, this[i - 1], preset = clear))
                    else -> it.link(EdgeLatch(clock, this[i - 1], clear))
                }
            }
        })
