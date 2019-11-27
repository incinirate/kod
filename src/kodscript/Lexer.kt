package kodscript

import java.io.PushbackInputStream

enum class TokenType {
    DIE_ROLL, // 3d6
    NUMBER,   // 4 + 8d4

    // Expression
    PLUS, MINUS, MULTIPLY,
    DIVIDE_DOWN, DIVIDE_UP,
    DOT, ASSIGN, EQUALS,

    // Functional
    OPEN_PAREN, CLOSE_PAREN,
    IDENTIFIER,

    FUNCTION
}

val reservedWords = mapOf(
    Pair("function", TokenType.FUNCTION)
)

data class Token(val type: TokenType, val source: String,
                 val line: Int, val column: Int)

private enum class State {
    BASE,

    NumberLike, DiceTransition, DiceRead,
    CheckIdent, InIdent,
    DivideCheck,
    EqualCheck
}

class ParseError(override val message: String,
                 val line: Int, val column: Int, val length: Int = 1): Throwable(message)

fun tokenizeScript(stream: PushbackInputStream): ArrayList<Token> {
    val tokens = ArrayList<Token>()

    var state: State = State.BASE
    val tokenParts = StringBuilder()

    var line = 1; var column = 0

    while (stream.available() > 0) {
        val char = stream.read().toChar()
        column += 1

        fun finishToken(tokenType: TokenType, keepChar: Boolean = false, transformer: ((String) -> TokenType?)? = null) {
            if (keepChar) tokenParts.append(char)
            else { stream.unread(char.toInt()); column -= 1 }

            state = State.BASE

            val newTokenType = transformer?.invoke(tokenParts.toString()) ?: tokenType
            tokens.add(Token(
                newTokenType, tokenParts.toString(),
                line, column - tokenParts.length + 1))

            tokenParts.clear() // Reset the builder for the next token
        }

        fun badCharacter(expected: String? = null) {
            throw ParseError("Unexpected character `$char` at line $line, column $column"
                + (expected?.let { ", expected $expected." } ?: "."), line, column)
        }

//        fun genCharAccept(c: Char, newState: State? = null): () -> Unit {
//            return {
//                when (char) {
//                    c -> {
//                        tokenParts.append(char)
//                        newState?.let { state = newState }
//                    }
//                    else -> badCharacter()
//                }
//            }
//        }

        when (state) {
            State.BASE -> {
                // Top level
                when (char) {
                    in '0' .. '9' -> {
                        tokenParts.append(char)
                        state = State.NumberLike
                    }

                    'd', 'D' -> {
                        tokenParts.append(char)
                        state = State.CheckIdent
                    }

                    in 'a' .. 'z', in 'A' .. 'Z', '_' -> {
                        tokenParts.append(char)
                        state = State.InIdent
                    }

                    '(' -> finishToken(TokenType.OPEN_PAREN, keepChar = true)
                    ')' -> finishToken(TokenType.CLOSE_PAREN, keepChar = true)
                    '.' -> finishToken(TokenType.DOT, keepChar = true)
                    '+' -> finishToken(TokenType.PLUS, keepChar = true)
                    '-' -> finishToken(TokenType.MINUS, keepChar = true)
                    '*' -> finishToken(TokenType.MULTIPLY, keepChar = true)
                    '/' -> { tokenParts.append(char); state = State.DivideCheck }

                    '=' -> { tokenParts.append(char); state = State.EqualCheck }

                    ' ', '\t', '\r' -> { /* Just ignore whitespace */ }
                    '\n' -> { line += 1; column = 0 }
                    else -> badCharacter()
                }
            }

            State.EqualCheck -> {
                // Is it an assignment or and equality check?
                when (char) {
                    '=' -> finishToken(TokenType.EQUALS, keepChar = true)
                    else -> finishToken(TokenType.ASSIGN)
                }
            }

            State.NumberLike -> {
                // Looks like a number, but it could be a dice roll
                when (char) {
                    in '0' .. '9' -> tokenParts.append(char)
                    'd', 'D' -> { tokenParts.append(char); state = State.DiceTransition }
                    else -> finishToken(TokenType.NUMBER)
                }
            }

            State.DiceTransition -> {
                // We've caught the 'dice' indicator, we need a dice type now
                when (char) {
                    in '0' .. '9' -> { tokenParts.append(char); state = State.DiceRead }
                    else -> badCharacter(expected = "number")
                }
            }

            State.DiceRead -> {
                // Now we accept more numbers but don't need them
                when (char) {
                    in '0' .. '9' -> tokenParts.append(char)
                    else -> finishToken(TokenType.DIE_ROLL)
                }
            }

            State.CheckIdent -> {
                when (char) {
                    in '0' .. '9' -> { tokenParts.append(char); state = State.DiceRead }
                    in 'a' .. 'z', in 'A' .. 'Z', '_' -> { tokenParts.append(char); state = State.InIdent }
                    else -> finishToken(TokenType.IDENTIFIER)
                }
            }

            State.InIdent -> {
                when (char) {
                    in '0' .. '9', in 'a' .. 'z',
                    in 'A' .. 'Z', '_' -> tokenParts.append(char)

                    else -> finishToken(TokenType.IDENTIFIER, transformer = { reservedWords[it] })
                }
            }

            State.DivideCheck -> {
                when (char) {
                    'd' -> finishToken(TokenType.DIVIDE_DOWN, keepChar = true)
                    'u' -> finishToken(TokenType.DIVIDE_UP, keepChar = true)
                    else -> badCharacter(expected = "`d` or `u`")
                }
            }
        }
    }

    return tokens
}
