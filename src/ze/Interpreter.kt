package ze

class Interpreter {

    fun interpret(expression: Expr?) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Ze.runtimeError(error)
        }
    }

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
                        (left as Double) - (right as Double)
                    }
                    TokenType.STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) * (right as Double)
                    }
                    TokenType.SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) / (right as Double)
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
                        -(right as Double)
                    }
                    TokenType.BANG -> !isTruthy(right)
                    else -> null
                }
            }

            null -> TODO()
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
}