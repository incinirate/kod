package kodscript

import java.util.*
import kotlin.collections.ArrayList

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



fun parseTokens(list: ArrayList<Token>): ASTNode {
    val tokens: Deque<Token> = ArrayDeque(list)

    while (tokens.isNotEmpty()) {
        val peekToken: Token = tokens.peekFirst()
        when (peekToken.type) {
            TokenType.IDENTIFIER -> parseReference(tokens)
        }
    }
}

fun parseReference(tokens: Deque<Token>) {

}
