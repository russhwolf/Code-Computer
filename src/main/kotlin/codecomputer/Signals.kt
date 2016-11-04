package codecomputer

import java.util.*

/**
 * A `Signal` is a combination of the [Readable] and [Writable] interfaces, so that it can both input and output a
 * boolean state
 */
interface Signal : Readable, Writable

/**
 * A `Readable` represents an item which has boolean state that can be [read][read] or [subscribed to][subscribe].
 */
interface Readable {
    /**
     * Returns the internal state of this [Readable]
     */
    fun read(): Boolean

    /**
     * Registers a function to receive updates to this [Readable]'s state
     */
    fun subscribe(subscriber: (Boolean) -> Unit): Unit = subscriber.invoke(read())

    /**
     * Unregisters an existing subscriber from this [Readable]
     */
    fun unsubscribe(subscriber: (Boolean) -> Unit) = Unit

    /**
     * Enables logging of this [Readable]'s state, printing the associated `tag` as part of the log.
     */
    fun log(tag: String?, list: List<Readable>? = null) = Unit
}

/**
 * A `Writable` represents an item which can have state [written][write] to it.
 */
interface Writable {
    /**
     * Writes the provided value to the internal state of this [Writable]
     */
    fun write(value: Boolean): Unit

    /**
     * Registers a subscriber (as in [subscribe]) which forwards the state of the given [Readable] to this [Writable]
     */
    fun link(that: Readable, transform: (Boolean) -> Boolean = { it }): Unit = that.subscribe { write(transform.invoke(that.read())) }
}

/**
 * The `Relay` class forms the backbone of this framework. In addition to holding a [Boolean] state exposed as a [Readable],
 * it contains the main implementation of the [Writable.write] method, which updates state and forwards it to a collection of
 * subscribers.
 *
 * Though it's not perfectly analogous, this class is intended to represent the telegraph relays first introduced in
 * chapter six of *Code*.
 */
class Relay(private var state: Boolean = false) : Signal {
    companion object {
        private val queue: Queue<(Boolean) -> Unit> = ArrayDeque()
        private var logCounter: Int = -1
        var logger: (String) -> Unit = ::println

        fun resetLogCount() {
            logCounter = -1
        }
    }

    private val subscribers: MutableList<(Boolean) -> Unit> = mutableListOf()
    private var tag: String? = null
    private var list: List<Readable>? = null

    override fun read(): Boolean = state

    override fun write(value: Boolean) {
        if (state == value) return
        state = value

        if (tag != null) {
            logState()
        }

        if (subscribers.isEmpty()) return

        val wasEmpty = queue.isEmpty()
        queue.addAll(subscribers)
        if (wasEmpty) {
            while (queue.isNotEmpty()) {
                queue.peek().invoke(state)
                queue.remove()
            }
        }
    }

    override fun subscribe(subscriber: (Boolean) -> Unit) {
        super.subscribe(subscriber)
        subscribers.add(subscriber)
    }

    override fun unsubscribe(subscriber: (Boolean) -> Unit) {
        super.unsubscribe(subscriber)
        subscribers.remove(subscriber)
    }

    override fun log(tag: String?, list: List<Readable>?) {
        this.tag = tag
        this.list = list
        logState()
    }

    private fun logState() = logger.invoke("${++logCounter}: $tag=$state${list?.let { " (${it.read()})" } ?: ""}")
}

/**
 * The `Constant` class implements [Readable] but not [Writable]. This makes it useful as a default input parameter to
 * avoid instantiating more [Relay] objects than necessary.
 */
enum class Constant() : Readable {
    FALSE, TRUE;

    override fun read() = this == TRUE
}

fun Int.toBits(size: Int): List<Boolean> = when {
    this < 0 -> (this + (1 shl size)).toBits(size)
    this shr size != 0 -> throw IllegalArgumentException("Overflow! Can't write $this in $size bits!")
    else -> (0 until size).map { this.nthBit(it) }
}

fun Int.nthBit(n: Int): Boolean = (this shr n) and 1 != 0

fun Int.toSignals(size: Int): List<Signal> = toBits(size).toSignals()

fun List<Boolean>.toInt(): Int = foldIndexed(0) { i, sum, b -> (if (b) 1 shl i else 0) + sum }

fun Boolean.toReadable(): Readable = if (this) Constant.TRUE else Constant.FALSE

fun List<Boolean>.toSignals(): List<Signal> = map(::Relay)

fun List<Boolean>.toReadables(): List<Readable> = map { it.toReadable() }

fun List<Readable>.toBits(): List<Boolean> = map { it.read() }

fun List<Readable>.read(): Int = toBits().toInt()

fun List<Readable>.read(index: Int): Boolean = this[index].read()

inline fun List<Readable>.subscribe(crossinline subscriber: (Int, Boolean) -> Unit)
        = forEachIndexed { i, readable -> readable.subscribe { subscriber(i, it) } }

fun List<Readable>.log(tag: String?) = forEachIndexed { i, readable -> readable.log(tag?.let { "$it[$i]" }, this) }

fun List<Writable>.write(index: Int, value: Boolean) = this[index].write(value)

fun List<Writable>.write(values: List<Boolean>) = values.forEachIndexed { i, value -> write(i, value) }

fun List<Writable>.write(value: Int) = write(value.toBits(size))

inline fun List<Writable>.link(readables: List<Readable>, crossinline transform: (Int, Boolean) -> Boolean)
        = forEachIndexed { i, writable -> writable.link(readables[i]) { transform.invoke(i, it) } }

fun List<Writable>.link(readables: List<Readable>) = link(readables, { i, b -> b })