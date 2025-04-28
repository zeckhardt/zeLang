package ze

class LangFunction(
    private val declaration: Stmt.Function
) : LangCallable {

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment: Environment = Environment(interpreter.globals)
        for (i in 0 until declaration.params.size) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.params.size
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}