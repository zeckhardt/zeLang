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
        }
    )

    fun register(globals: Environment) {
        functions.forEach { (name, callable) ->
            globals.define(name, callable)
        }
    }
}
