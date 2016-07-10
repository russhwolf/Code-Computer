package codecomputer

/**
 * A `HalfAdder` returns the least significant digit of the binary sum of the two inputs, as well as a `carry` flag indicating
 * whether  there is overflow
 *
 * This class represents the Half Adder described on page 137 of *Code*
 */
class HalfAdder(a: Readable, b: Readable) : Readable by a xor b {
    val carry: Readable = a and b
}

/**
 * A `FullAdder` returns the least significant digit of the binary sum of the two inputs, as well as a `carry` inputs
 * and outputs indicating upstream or downstream overflow.
 *
 * This class represents the Full Adder described on page 138 of *Code*
 */
class FullAdder private constructor(carry: Readable, a: Readable, b: Readable, delegate: Signal) : Readable by delegate {
    val carry: Readable

    constructor(carry: Readable, a: Readable, b: Readable) : this(carry, a, b, Relay())

    init {
        val halfAdder1 = HalfAdder(a, b)
        val halfAdder2 = HalfAdder(carry, halfAdder1)

        delegate.link(halfAdder2)
        this.carry = halfAdder1.carry or halfAdder2.carry
    }
}

/**
 * A `MultiAdder` connects multiple [FullAdder]s via their carry inputs and outputs.
 *
 * This class represents the multi-bit Adder described on page 140 of *Code*
 */
class MultiAdder private constructor(carry: Readable, a: List<Readable>, b: List<Readable>, delegate: List<Signal>) :
        List<Readable> by delegate {
    val carry: Readable

    constructor(carry: Readable, a: List<Readable>, b: List<Readable>) : this(carry, a, b, a.zip(b).map { Relay() })

    init {
        var c = carry
        val adders = indices.map {
            val adder = FullAdder(c, a[it], b[it])
            c = adder.carry
            adder
        }

        delegate.link(adders)
        this.carry = adders.last().carry
    }
}

/**
 * A `OnesComplement` inverts the `input` signals if `invert` is true, or leaves them unchanged if `invert` is false.
 *
 * This class represents the One's Complement described on page 150 of *Code*
 */
class OnesCompliment(invert: Readable, input: List<Readable>) :
        List<Readable> by (input.map { invert xor it })

/**
 * An `AddSubtract` can perform addition or subtraction of the input signals, depending on the `sub` input.
 *
 * This class represents the addition/subtration machine described in chapter thirteen of *Code*
 */
class AddSubtract private constructor(sub: Readable, a: List<Readable>, b: List<Readable>, delegate: List<Signal>) :
        List<Readable> by delegate {
    val overflow: Readable

    constructor(sub: Readable, a: List<Readable>, b: List<Readable>) : this(sub, a, b, a.zip(b).map { Relay() })

    init {
        val adder = MultiAdder(sub, a, OnesCompliment(sub, b))

        delegate.link(adder)
        overflow = sub xor adder.carry
    }
}