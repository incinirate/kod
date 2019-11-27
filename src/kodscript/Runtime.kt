package kodscript

import util.Either
import util.orElse
import util.then
import kotlin.random.Random

object Runtime: KodObject {
    override val name: String = "_G"
    override val exposedProperties: Map<String, Any>
        get() = mapOf()
}

class RuntimeError(override val message: String) : Throwable(message)


class DirectReference(private val context: KodObject,
                      private val name: String) {

    fun get(): Any {
        return context.exposedProperties.getOrElse(name)
            { throw RuntimeError("Attempting to reference non-existent property $name of ${context.name}") }
    }
}

class Reference(private val context: Reference?,
                private val name: String) : LeftASTNode() {

    override fun performOperation(): DirectReference {
        val upRef: KodObject? = context?.performOperation()?.get()?.also {
            (it is KodObject).orElse { throw RuntimeError("Attempting to reference inside property `${context.name}`(${it.javaClass.name}) of ${context.context?.name ?: "_G"}") } }
                as KodObject?

        val ctx: KodObject = upRef ?: Runtime
        return DirectReference(ctx, name)
    }
}

class DiceRoll(private val count: Int,
               private val diceSize: Int) : RightASTNode() {

    override fun performOperation(): Int {
        return (1 .. count).fold(0, { acc, _ ->
            acc + Random.Default.nextInt(diceSize) + 1
        })
    }
}

class Number(private val value: Int) : RightASTNode() {

    override fun performOperation(): Int {
        return value
    }
}


// Operational Classes
class PlusOP(override val lhs: RightASTNode,
             override val rhs: RightASTNode) : PureBinaryOperation() {

    override fun performOperation(): Any {
        val lval: Any = lhs.performOperation()
        val rval: Any = rhs.performOperation()

        when {
            lval is Int && rval is Int -> return lval + rval

        }

        throw RuntimeError("Objects (${lval.javaClass.name}, ${rval.javaClass.name}) are not eligible for addition")
    }
}

class UnaryPlus(override val term: RightASTNode): PureUnaryOperation() {

    override fun performOperation(): Any {
        when (val value: Any = term.performOperation()) {
            is Int -> return +value
        }

        throw RuntimeError("Object (${term.javaClass.name} is not eligible for unary plus")
    }
}

class UnaryMinus(override val term: RightASTNode): PureUnaryOperation() {

    override fun performOperation(): Any {
        when (val value: Any = term.performOperation()) {
            is Int -> return -value
        }

        throw RuntimeError("Object (${term.javaClass.name} is not eligible for unary minus")
    }
}
