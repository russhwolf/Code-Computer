package codecomputer

/**
 * A `Not` reads as the opposite of its `input` [Readable]
 *
 * This class represents the inverter first described on page 119 of *Code*
 */
class Not(input: Readable) : Readable by (Relay().apply { link(input) { !it } })

operator fun Readable.not(): Readable = Not(this)

/**
 * This class contains the shared implementation details of the [And] and [Or] gates. It takes two inputs and combines
 * them according to the provided boolean operator
 *
 * @see And
 * @see Or
 */
open class CombinedSignal(a: Readable, b: Readable, combine: (Boolean, Boolean) -> Boolean) :
        Readable by combinedSignalDelegate(a, b, combine)

private inline fun combinedSignalDelegate(a: Readable, b: Readable, crossinline combine: (Boolean, Boolean) -> Boolean): Readable = when {
    a is Constant && b is Constant -> combine(a.read(), b.read()).toReadable()
    else -> Relay().apply {
        link(a) { combine(it, b.read()) }
        link(b) { combine(a.read(), it) }
    }
}

/**
 * This class contains the shared implementation details of the [MultiAnd] and [MultiOr] classes, acting as the multiple-input
 * equivalent of the [CombinedSignal] class. It takes a list of input [Readable]s and combines them according to the provided
 * operator.
 *
 * @see MultiAnd
 * @see MultiOr
 */
open class MultiCombinedSignal(input: List<Readable>, combine: (Readable, Readable) -> Readable) :
        Readable by multiCombinedSignalDelegate(input, combine)

private inline fun multiCombinedSignalDelegate(input: List<Readable>, combine: (Readable, Readable) -> Readable)
        = (input.reduce { a, b -> combine(a, b) })

/**
 * An `And` gate [read][Readable]s as `true` if and only if both input signals are `true`.
 *
 * This class represents the AND gate described on page 113 of *Code*
 *
 * @see MultiAnd
 */
class And(a: Readable, b: Readable) : Readable by CombinedSignal(a, b, { p, q -> p && q })

infix fun Readable.and(that: Readable): Readable = And(this, that)

/**
 * A `MultiAnd` gate [read][Readable]s as `true` if and only if all input signals are `true`.
 *
 * @see And
 */
class MultiAnd(input: List<Readable>) : Readable by MultiCombinedSignal(input, ::And) {
    constructor(vararg input: Readable) : this(input.toList())
}

/**
 * An `Or` gate [read][Readable]s as `true` if at least one of the input signals is `true`.
 *
 * This class represents the OR gate described on page 118 of *Code*
 *
 * @see MultiOr
 */
class Or(a: Readable, b: Readable) : Readable by CombinedSignal(a, b, { p, q -> p || q })

infix fun Readable.or(that: Readable): Readable = Or(this, that)

/**
 * A `MultiOr` gate [read][Readable]s as `true` if at least one of the input signals is `true`.
 *
 * @see Or
 */
class MultiOr(input: List<Readable>) : Readable by MultiCombinedSignal(input, ::Or) {
    constructor(vararg input: Readable) : this(input.toList())
}

/**
 * A `Nand` gate [read][Readable]s as `true` if at least one of the input signals is `false`.
 *
 * This class represents the NAND gate described on page 127 of *Code*
 *
 * @see MultiNand
 */
class Nand(a: Readable, b: Readable) : Readable by !(a and b)

infix fun Readable.nand(that: Readable): Readable = Nand(this, that)

/**
 * A `MultiNand` gate [read][Readable]s as `true` if at least one of the input signals is `false`.
 *
 * @see Nand
 */
class MultiNand(input: List<Readable>) : Readable by !MultiAnd(input) {
    constructor(vararg input: Readable) : this(input.toList())
}

/**
 * A `Nor` gate [read][Readable]s as `true` if and only if both input signals are `false`.
 *
 * This class represents the NOR gate described on page 124 of *Code*
 *
 * @see MultiNor
 */
class Nor(a: Readable, b: Readable) : Readable by !(a or b)

infix fun Readable.nor(that: Readable): Readable = Nor(this, that)

/**
 * A `MultiNor` gate [read][Readable]s as `true` if and only if all input signals are `false`.
 *
 * @see Nor
 */
class MultiNor(input: List<Readable>) : Readable by !MultiOr(input) {
    constructor(vararg input: Readable) : this(input.toList())
}

/**
 * A `Xor` gate [read][Readable]s as `true` if exactly one of the input signals is true.
 *
 * This class represents the XOR gate described on page 136 of *Code*
 */
class Xor(a: Readable, b: Readable) : Readable by (a or b) and (a nand b)

infix fun Readable.xor(that: Readable): Readable = Xor(this, that)