package kodscript

import util.loop
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction2

abstract class ASTNode {
    abstract fun performOperation(): Any
}

abstract class RightASTNode : ASTNode()
abstract class LeftASTNode : RightASTNode()

abstract class PureBinaryOperation : RightASTNode() {
    abstract val lhs: RightASTNode
    abstract val rhs: RightASTNode
}

abstract class MutativeBinaryOperation : RightASTNode() {
    abstract val lhs: LeftASTNode
    abstract val rhs: RightASTNode
}

abstract class BaseUnaryOperation: RightASTNode() {}

abstract class PureUnaryOperation: BaseUnaryOperation() {
    abstract val term: RightASTNode
}

abstract class MutativeUnaryOperation: BaseUnaryOperation() {
    abstract val term: LeftASTNode
}

fun badToken(token: Token, expected: String? = null): Nothing {
    throw ParseError("Unexpected token `${token.source}`(${token.type}) at line ${token.line}, column ${token.column}"
            + (expected?.let { ", expected $expected." } ?: "."), token.line, token.column)
}

fun parseTokens(list: ArrayList<Token>): ASTNode {
    val tokens: Deque<Token> = ArrayDeque(list)

//    while (tokens.isNotEmpty()) {
//        val peekToken: Token = tokens.peekFirst()
//        when (peekToken.type) {
//            TokenType.IDENTIFIER -> parseRootReference(tokens)
//        }
//    }
    return parseExpression(tokens)
}

enum class Associativity { LEFT, RIGHT }
data class OperatorData(
    val constructor: KFunction2<RightASTNode, RightASTNode, RightASTNode>, val precedence: Int, val associativity: Associativity)
val operatorMap = mapOf<TokenType, OperatorData>(
    Pair(TokenType.PLUS, OperatorData(::PlusOP, 1, Associativity.RIGHT))
)
fun parseExpression(tokens: Deque<Token>, withParen: Boolean = false): RightASTNode {
    val termStack: Stack<RightASTNode> = Stack()
    val opStack: Stack<OperatorData> = Stack()

    fun popOpStack() {
        val op = opStack.pop()
        val rhs = termStack.pop(); val lhs = termStack.pop()
        termStack.push(op.constructor.invoke(lhs, rhs))
    }

    var lastWasTerm = false
    loop {
        val topToken: Token = tokens.removeFirst()
        when (topToken.type) {
            TokenType.OPEN_PAREN -> termStack.push(parseExpression(tokens, withParen = true))
            TokenType.CLOSE_PAREN -> {
                if (withParen) return@loop
                else badToken(topToken)
            }

            else -> {
                // Check if it's an operator
                val operator = operatorMap[topToken.type]
                if (operator != null) {
                    if (!lastWasTerm) badToken(topToken, "term")

                    val topOfStack = opStack.peek()
                    while ((topOfStack.precedence >  operator.precedence)
                        || (topOfStack.precedence == operator.precedence && operator.associativity == Associativity.LEFT)) {

                        popOpStack()
                    }

                    opStack.push(operator)
                    lastWasTerm = false
                } else {
                    // Otherwise it must be a term
                    tokens.addFirst(topToken) // Add it back so we can defer the term parsing

                    if (lastWasTerm) {
                        // Not part of the same expression
                        return@loop
                    }

                    termStack.push(parseFullTerm(tokens))
                    lastWasTerm = true
                }
            }
        }
    }

    while (opStack.isNotEmpty()) {
        popOpStack()
    }

    if (termStack.size > 1) error("parseExpression finished with more than 1 term in the termStack")
    return termStack.pop()
}

fun parseFullTerm(tokens: Deque<Token>): RightASTNode {
    val unaryOperator: Token = tokens.removeFirst()
    return when (unaryOperator.type) {
        TokenType.PLUS -> UnaryPlus(parsePartialTerm(tokens))
        TokenType.MINUS -> UnaryMinus(parsePartialTerm(tokens))
        else -> {
            tokens.addFirst(unaryOperator) // Wasn't actually a unary operator
            parsePartialTerm(tokens) // Just do it normally
        }
    }
}

fun parsePartialTerm(tokens: Deque<Token>): RightASTNode {
    val peekToken: Token = tokens.peekFirst()
    return when (peekToken.type) {
        TokenType.IDENTIFIER -> parseRootReference(tokens)
        TokenType.NUMBER -> Number(tokens.removeFirst().source.toInt())
        TokenType.DIE_ROLL -> {
            val parts = tokens.removeFirst().source.split()
            DiceRoll()
        }
        else -> badToken(peekToken)
    }
}

// A reference at the beginning of the statement is owned by two statement types
// - An assignment (lhv)
// - A function call (rhv)
// - An untouched pure reference (lhv)
fun parseRootReference(tokens: Deque<Token>): RightASTNode {
    var currentReference = Reference(null, tokens.removeFirst().source)

    while (tokens.peekFirst().type == TokenType.DOT) {
        tokens.removeFirst() // Discard the dot
        currentReference = Reference(currentReference, tokens.removeFirst().source)
    }

    val nextToken = tokens.peekFirst()
    when (nextToken.type) {
        TokenType.OPEN_PAREN -> TODO("Function Call")
        TokenType.ASSIGN -> {
            tokens.removeFirst() // Discard the assignment operator
            TODO("ASSIGN")
        }
        else -> return currentReference
    }
}

//fun parseReference(): Reference {
//
//}
