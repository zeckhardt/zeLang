package ze

class LangInstance(
    private var klass: LangClass
) {
    private val fields = HashMap<String, Any?>()

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme)
        }

        val method = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields.put(name.lexeme, value)
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}