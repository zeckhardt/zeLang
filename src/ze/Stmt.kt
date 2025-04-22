package ze

sealed class Stmt {
    data class Print(val expression: Expr) : Stmt()
    data class Expression(val expression: Expr) :  Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
}