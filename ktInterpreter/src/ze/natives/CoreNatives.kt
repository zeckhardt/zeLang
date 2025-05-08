package ze.natives

import ze.Environment
import ze.Interpreter
import ze.LangCallable

object CoreNatives {
    val functions: Map<String, LangCallable> = mapOf(
        "clock" to object : LangCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString(): String = "<native fn>"
        },

        "type" to object : LangCallable {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val value = arguments.firstOrNull()
                return when (value) {
                    null -> "none"
                    is Double -> "number"
                    is String -> "string"
                    is Boolean -> "bool"
                    is LangCallable -> "function"
                    else -> "unknown"
                }
            }

            override fun toString(): String {
                return "<native fn type>"
            }
        }
    )

    fun register(globals: Environment) {
        functions.forEach { (name, callable) ->
            globals.define(name, callable)
        }
    }
}
