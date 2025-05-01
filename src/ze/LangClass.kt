package ze

class LangClass(
    val name: String,
    val methods: Map<String, LangFunction>
) : LangCallable  {

    fun findMethod(name: String): LangFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }

    override fun toString(): String {
        return name
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LangInstance(this)
        val initializer: LangFunction? = findMethod("init")

        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    override fun arity(): Int {
        val initializer: LangFunction? = findMethod("init")
        if (initializer == null) return 0
        return initializer.arity()
    }
}