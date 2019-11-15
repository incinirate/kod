import kodscript.DiceRoll
import kodscript.Number
import kodscript.PlusOP
import kodscript.tokenizeScript
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
        }
    }
}
