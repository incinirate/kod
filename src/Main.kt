import kodscript.*
import kodscript.Number
import util.loop
import java.io.InputStream
import java.io.PushbackInputStream

fun main() {
    println("Value: ${PlusOP(DiceRoll(3, 6), Number(9)).performOperation()}")

    loop {
        print("> ")
        val line = readLine()?.plus('\n')
        if (line != null) {
            val tokens = tokenizeScript(PushbackInputStream(line.byteInputStream()))
            println("Tokens: $tokens")

            val tree = parseTokens(tokens)
            println("AST: $tree")
        }
    }
}
