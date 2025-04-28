package ze

class LangFunction(
    private val name: String?,
    private val declaration: Expr.Function,
    private val closure: Environment
) : LangCallable {

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
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
        if (name == null) return "<fn>"
        return "<fn $name>"
    }
}