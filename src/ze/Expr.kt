package ze

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Assign(val name: Token, val value: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Variable(val name: Token) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Comma(val expressions: List<Expr>) : Expr()
    data class Conditional(val condition: Expr, val thenBranch: Expr, val elseBranch: Expr) : Expr()
}