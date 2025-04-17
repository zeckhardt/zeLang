package ze

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Scanner
import kotlin.system.exitProcess

object Ze {
    var hadError = false

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
        var tokens = scanner.scanToken()

        for (token in tokens) {
            println(token)
        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}