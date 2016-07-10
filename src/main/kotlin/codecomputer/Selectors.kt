package codecomputer

/**
 * A `Select` outputs the `a` input if `select` is false or the `b` input if select is true.
 *
 * This class represents the *2-Line-to-1-Line Selector* on page 169 of *Code*
 *
 * @see MultiSelector
 */
class Selector(select: Readable, a: Readable, b: Readable) : Readable by (select and b) or (!select and a)

/**
 * A `MultiSelector` reads out a value in the `data` input indicated by the `select` input. The `data` input should have
 * length equal to `2^N`, where `N` is the length of the `select` input.
 *
 * If the `select` input has length `1` and the `data` input has length `2`, this reduces to the [Selector] class.
 *
 * This class represents the *8-Line-to-1-Line Data Selector* on page 194 of *Code*.
 */
class MultiSelector(select: List<Readable>, data: List<Readable>) :
        Readable by InnerSelectorDecoder(select, data).let(::MultiOr)

/**
 * A `MultiDecoder` takes in a `data` input and returns a list where the output addressed by the `select` input is equal
 * to `data`, and all other outputs are false. The output will have length equal to `2^N`, where `N` is the length of
 * the `select` input.
 *
 * This class represents the *3-to-8 Decoder* presented on page 197 of *Code*.
 */
class MultiDecoder(select: List<Readable>, data: Readable) :
        List<Readable> by InnerSelectorDecoder(select, (1..(1 shl select.size)).map { data })

private class InnerSelectorDecoder(selection: List<Readable>, data: List<Readable>) :
        List<Readable> by (data.mapIndexed {
            i, dataBit ->
            dataBit and MultiAnd(selection.mapIndexed {
                j, selectionBit ->
                if ((i / (1 shl j)) % 2 == 0) !selectionBit else selectionBit
            })
        })