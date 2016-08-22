package codecomputer

/**
 * An `Accumulator` adds the integer representation of its output to that of the `input` list every time the `add` input
 * changes from `false` to `true`
 *
 * This class represents the configuration shown on page 170 of *Code*
 */
class Accumulator(input: List<Readable>, add: Readable, clear: Readable) :
        List<Readable> by (input.map { Relay() }.apply {
            MultiAdder(Constant.FALSE, input, this)
                    .let { MultiEdgeLatch(add, it, clear) }
                    .let { this.link(it) }
        })

/**
 * A `RamAccumulator` reads through items stored in a [ram array][ControlPanelRam] and adds them together. The output
 * is a running total of numbers pulled from the ram array as the `clock` input toggles between `false` and `true`.
 *
 * This class represents the configuration shown on page 208 of *Code*
 */
class RamAccumulator(clock: Readable, address: List<Readable>, data: List<Readable>, write: Readable, clear: Readable,
                     takeover: Readable) :
        List<Readable> by (RippleCounter(address.size, clock, clear)
                .let {
                    ControlPanelRam(
                            it,
                            data.map { Constant.FALSE },
                            Constant.FALSE,
                            takeover,
                            address,
                            data,
                            write)
                }
                .let { Accumulator(it, clock, clear) })
