package ze

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Ze {
    var hadError = false
    var hadRuntimeError = false
    private val interpreter: Interpreter = Interpreter()

    @JvmStatic
    fun main(args: Array<String>) {
        when {
            args.size > 1 -> {
                println("Usage: zek [script]")
                exitProcess(64)
            }
            args.size == 1 -> runFile(args[0])
            else -> runPrompt()
        }
    }

    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        val source = String(bytes, Charset.defaultCharset())
        rn(source)

        if (hadError) exitProcess(64)
        if (hadRuntimeError) exitProcess(70)
    }

    private fun runPrompt() {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            print("->")
            val line = reader.readLine() ?: break
            rn(line)
            hadError = false
        }
    }

    private fun rn(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val parser = Parser(tokens)
        val statements = parser.parse()

        // Stop if there was a syntax error
        if (hadError) return

        val resolver = Resolver(interpreter)
        statements.forEach { resolver.resolve(it) }

        // Stop if there was a resolution error
        if (hadError) return

        interpreter.interpret(statements)

    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message} \n[${error.token.line}]")
        hadRuntimeError = true
    }
}