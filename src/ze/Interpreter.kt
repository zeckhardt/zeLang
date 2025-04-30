package ze


class Interpreter {
    private class BreakException : RuntimeException()
    private class ContinueException: RuntimeException()
    val globals: Environment = Environment()
    private var environment = globals
    private val locals = HashMap<Expr, Int>()

    // native functions
    init {
        globals.define("clock", object : LangCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString(): String = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Ze.runtimeError(error)
        }
    }

    // Expressions
    fun evaluate(expr: Expr?): Any? {
        return when (expr) {
            is Expr.Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)

                return when(expr.operator.type) {
                    TokenType.GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) > (right as Double)
                    }
                    TokenType.GREATER_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) >= (right as Double)
                    }
                    TokenType.LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) < (right as Double)
                    }
                    TokenType.LESS_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) <= (right as Double)
                    }
                    TokenType.BANG_EQUAL -> return !isEqual(left, right)
                    TokenType.EQUAL_EQUAL -> return isEqual(left, right)
                    TokenType.PLUS -> {
                        if (left is Double && right is Double) {
                            return left + right
                        }

                        if (left is String && right is String) {
                            return left + right
                        }

                        if (left is String && right is Double) {
                            return left + right.toString()
                        }

                        if (left is Double && right is String) {
                            return left.toString() + right
                        }

                        throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                    }
                    TokenType.MINUS -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) - (right as Double)
                    }
                    TokenType.STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        return (left as Double) * (right as Double)
                    }
                    TokenType.SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        if (right == 0) {
                            throw RuntimeError(expr.operator, "Divide by zero.")
                        }
                        return (left as Double) / (right as Double)
                    }
                    else -> null
                }
            }

            is Expr.Literal -> expr.value

            is Expr.Grouping -> evaluate(expr.expression)

            is Expr.Unary -> {
                val right = evaluate(expr.right)
                return when (expr.operator.type) {
                    TokenType.MINUS -> {
                        checkNumberOperand(expr.operator, right)
                        return -(right as Double)
                    }
                    TokenType.BANG -> !isTruthy(right)
                    else -> null
                }
            }

            is Expr.Variable -> {
                return lookupVariable(expr.name, expr)
            }

            is Expr.Assign -> {
                val value: Any? = evaluate(expr.value)

                val distance = locals[expr]
                if (distance != null) {
                    environment.assignAt(distance, expr.name, value)
                } else {
                    globals.assign(expr.name, value)
                }

                return value
            }

            is Expr.Comma -> {
                var result: Any? = null

                for (subExpr in expr.expressions) {
                    result = evaluate(subExpr)
                }

                return result
            }

            is Expr.Conditional -> {
                val conditionalValue: Any? = evaluate(expr.condition)

                return if (isTruthy(conditionalValue)) {
                    evaluate(expr.thenBranch)
                } else {
                    evaluate(expr.elseBranch)
                }
            }

            is Expr.Logical -> {
                val left: Any? = evaluate(expr.left)

                if (expr.operator.type == TokenType.OR) {
                    if (isTruthy(left)) return left
                } else {
                    if (!isTruthy(left)) return left
                }

                return evaluate(expr.right)
            }

            is Expr.Call -> {
                val callee: Any? = evaluate(expr.callee)

                val arguments = ArrayList<Any?>()
                for (argument in expr.arguments) {
                    arguments.add(argument)
                }

                if (callee !is LangCallable) {
                    throw RuntimeError(expr.paren, "Can only call functions and classes.")
                }

                val function: LangCallable = callee
                if (arguments.size != function.arity()) {
                    throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
                }

                return function.call(this, arguments)
            }

            is Expr.Function -> {
                return LangFunction(null, expr, environment)
            }

            null -> null
        }
    }

    // Statements
    private fun execute(stmt: Stmt?): Void? {
        when (stmt) {
            is Stmt.Expression -> {
                evaluate(stmt.expression)
            }

            is Stmt.Print -> {
                val value = evaluate(stmt.expression)
                println(stringify(value))
            }

            is Stmt.Var -> {
                var value: Any? = null
                if (stmt.initializer != null) {
                    value = evaluate(stmt.initializer)
                }

                environment.define(stmt.name.lexeme, value)
            }

            is Stmt.Block -> {
                executeBlock(stmt.statements, Environment(environment))
            }

            is Stmt.If -> {
                if (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.thenBranch)
                } else if (stmt.elseBranch != null) {
                    execute(stmt.elseBranch)
                }
            }

            is Stmt.While -> {
                while (isTruthy(evaluate(stmt.condition))) {
                    try {
                        execute(stmt.body)
                    } catch (_: ContinueException) {
                        continue
                    } catch (_: BreakException) {
                        break
                    }
                }
            }

            is Stmt.Break -> {
                throw BreakException()
            }

            is Stmt.Continue -> {
                throw ContinueException()
            }

            is Stmt.Function -> {
                val fnName: String = stmt.name.lexeme
                environment.define(fnName, LangFunction(fnName, stmt.function, environment))
            }

            is Stmt.Return -> {
                var value: Any? = null
                if (stmt.value != null) {
                    value = evaluate(stmt.value)
                }

                throw Return(value)
            }

            null -> null
        }
        return null
    }

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment) {
        val previous: Environment = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "none"

        if (obj is Double) {
            var text: String = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return obj.toString()
    }

    private fun lookupVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }
}