package ze

sealed class Stmt {
    data class Print(val expression: Expr) : Stmt()
    data class Expression(val expression: Expr) : Stmt()
    data class Var(val name: Token, val initializer: Expr?) : Stmt()
    data class Block(val statements: List<Stmt?>) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
    data class While(val condition: Expr, val body: Stmt) : Stmt()
    class Break : Stmt()
    class Continue : Stmt()
    data class Function(val name: Token, val function: Expr.Function) : Stmt()
    data class Return(val keyword: Token, val value: Expr?) : Stmt()
    data class Class(val name: Token, val methods: List<Function>) : Stmt()
}