package codecomputer

/**
 * This `Ram` class represents a single unit of RAM, which stores multiple bits of data which are indexed by the `address` input.
 * The `data` input is written to the internal value addressed by the `address` input when the `write` input changes from
 * `false` to `true`. The output gives the current value stored at the location indicated by `address`.
 *
 * This class replicates the RAM configuration shown on page 1998 of *Code*.
 *
 * @see MultiRam
 */
class Ram(address: List<Readable>, data: Readable, write: Readable) :
        Readable by (MultiDecoder(address, write)
                .map { LevelLatch(it, data) }
                .let { MultiSelector(address, it) })

/**
 * A `MultiRam` instance is an array of multiple [Ram] instances, corresponding to the `data` inputs.
 *
 * This class represents the RAM array shown on page 201 of *Code*
 *
 * @see Ram
 * @see ControlPanelRam
 */
class MultiRam(address: List<Readable>, data: List<Readable>, write: Readable) :
        List<Readable> by (data.map { Ram(address, it, write) })

/**
 * A `ControlPanelRam` provides extra inputs to [MultiRam] so that the internal state can be adjusted from two different
 * sources.
 *
 * This class represents the RAM/control panel configuration described on page 204 of *Code*
 *
 * @see MultiRam
 */
class ControlPanelRam(address: List<Readable>, data: List<Readable>, write: Readable, takeover: Readable,
                      addressOverride: List<Readable>, dataOverride: List<Readable>, writeOverride: Readable) :
        List<Readable> by MultiRam(
                address.indices.map { Selector(takeover, address[it], addressOverride[it]) },
                data.indices.map { Selector(takeover, data[it], dataOverride[it]) },
                Selector(takeover, write, writeOverride)
        )
