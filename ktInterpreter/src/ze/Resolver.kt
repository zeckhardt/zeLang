package ze

class Resolver(
    private val interpreter: Interpreter
) {
    private enum class FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER
    }

    private class Variable(
        val name: Token, var state: VariableState
    )

    private enum class VariableState {
        DECLARED,
        DEFINED,
        READ
    }

    private enum class ClassType {
        NONE,
        CLASS
    }

    private val scopes = ArrayDeque<MutableMap<String, Variable>>()
    private var insideLoop: Int = 0
    private var currentFunction = FunctionType.NONE
    private var currentClass: ClassType = ClassType.NONE

    fun resolve(stmt: Stmt?): Void? {
        when (stmt) {
            is Stmt.Block -> {
                beginScope()
                stmt.statements.forEach { resolve(it) }
                endScope()
            }

            is Stmt.Var -> {
                declare(stmt.name)
                if (stmt.initializer != null) {
                    resolve(stmt.initializer)
                }
                define(stmt.name)
            }

            is Stmt.Function -> {
                declare(stmt.name)
                define(stmt.name)
                resolveFunction(stmt.function, FunctionType.FUNCTION)
            }

            is Stmt.Expression -> {
                resolve(stmt.expression)
            }

            is Stmt.If -> {
                resolve(stmt.condition)
                resolve(stmt.thenBranch)
                if (stmt.elseBranch != null) resolve(stmt.elseBranch)
            }

            is Stmt.Print -> {
                resolve(stmt.expression)
            }

            is Stmt.Return -> {
                if (currentFunction == FunctionType.NONE) {
                    Ze.error(stmt.keyword, "Can't return from top-level code.")
                }

                if (stmt.value != null) {
                    if (currentFunction == FunctionType.INITIALIZER) {
                        Ze.error(stmt.keyword, "Can't return a value from an initializer.")
                    }

                    resolve(stmt.value)
                }
            }

            is Stmt.While -> {
                resolve(stmt.condition)
                insideLoop++
                resolve(stmt.body)
                insideLoop--
            }

            is Stmt.Break, is Stmt.Continue -> {
                if (insideLoop == 0) {
                    error("Can't use ${if (stmt is Stmt.Break) "break" else "continue"} outside of a loop.")
                }
            }

            is Stmt.Class -> {
                val enclosingClass: ClassType = currentClass
                currentClass = ClassType.CLASS

                declare(stmt.name)
                define(stmt.name)

                beginScope()
                scopes.first().put("this", Variable(stmt.name, VariableState.DEFINED))

                for (method: Stmt.Function in stmt.methods) {
                    var declaration: FunctionType = FunctionType.METHOD
                    if (method.name.lexeme == "init") {
                        declaration = FunctionType.INITIALIZER
                    }
                    resolveFunction(method.function, declaration)
                }

                endScope()
                currentClass = enclosingClass
            }

            null -> null
        }
        return null
    }

    fun resolve(expr: Expr): Void? {
        when (expr) {
            is Expr.Variable -> {
                if (scopes.isEmpty() && scopes.first().containsKey(expr.name.lexeme) &&
                    scopes.first()[expr.name.lexeme]?.state == VariableState.DECLARED) {
                    Ze.error(expr.name, "Can't read local variable in its own initializer.")
                }

                resolveLocal(expr, expr.name, true)
            }

            is Expr.Assign -> {
                resolve(expr.value)
                resolveLocal(expr, expr.name, false)
            }

            is Expr.Function -> {
                resolveFunction(expr, FunctionType.FUNCTION)
            }

            is Expr.Binary -> {
                resolve(expr.left)
                resolve(expr.right)
            }

            is Expr.Call -> {
                resolve(expr.callee)

                for (argument in expr.arguments) {
                    resolve(argument)
                }
            }

            is Expr.Grouping -> {
                resolve(expr.expression)
            }

            is Expr.Literal -> {
                return null
            }

            is Expr.Logical -> {
                resolve(expr.right)
                resolve(expr.left)
            }

            is Expr.Unary -> {
                resolve(expr.right)
            }

            is Expr.Comma -> {
                for (expr in expr.expressions) {
                    resolve(expr)
                }
            }

            is Expr.Conditional -> {
                resolve(expr.condition)
                resolve(expr.thenBranch)
                resolve(expr.elseBranch)
            }

            is Expr.Get -> {
                resolve(expr.obj)
            }

            is Expr.Set -> {
                resolve(expr.value)
                resolve(expr.obj)
            }

            is Expr.This -> {
                if (currentClass == ClassType.NONE) {
                    Ze.error(expr.keyword, "Can't use 'this' outside of a class.")
                    return null
                }
                resolveLocal(expr, expr.keyword, false)
            }
        }

        return null
    }

    private fun beginScope() {
        scopes.addFirst(HashMap())
    }

    private fun endScope() {
        val scope = scopes.removeFirst()

        for ((_, variable) in scope) {
            if (variable.state == VariableState.DEFINED) {
                Ze.error(variable.name, "Local variable is not used.")
            }
        }
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope: MutableMap<String, Variable> = scopes.first()
        if (scope.containsKey(name.lexeme)) {
            Ze.error(name, "Already a variable with this name in this scope.")
        }

        scope.put(name.lexeme, Variable(name, VariableState.DECLARED))
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.first().put(name.lexeme, Variable(name, VariableState.DEFINED))
    }

    private fun resolveLocal(expr: Expr, name: Token, isRead: Boolean) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)

                // Mark it used.
                if (isRead) {
                    scopes[i][name.lexeme]?.state = VariableState.READ
                }
                return
            }
        }
        // Not found. Assume it is global.
    }

    private fun resolveFunction(function: Expr.Function, type: FunctionType) {
        val enclosingFunction: FunctionType = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        for (statement in function.body) {
            resolve(statement)
        }
        endScope()
        currentFunction = enclosingFunction
    }
}