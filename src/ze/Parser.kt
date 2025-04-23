package ze

class Parser(
    private val tokens: List<Token>
) {
    private var curr: Int = 0
    private class ParseError: RuntimeException()
    private var loopDepth: Int = 0

    fun parse(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    /**
     * declaration -> varDecl | statement ;
     */
    private fun declaration(): Stmt? {
        try {
            if (match(TokenType.VAR)) return varDeclaration()

            return statement()
        } catch (_: ParseError) {
            synchronize()
            return null
        }
    }

    /**
     * varDecl-> "var" IDENTIFIER ( "=" expression )? ";" ;
     */
    private fun varDeclaration(): Stmt {
        val name: Token = consume(TokenType.IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    /**
     * statement -> exprStmt | printStmt | ifStmt | forStmt | whileStmt | breakStmt | block;
     */
    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.WHILE)) return whileStatement()
        if (match(TokenType.FOR)) return forStatement()
        if (match(TokenType.PRINT)) return printStatement()
        if (match(TokenType.BREAK)) return breakStatement()
        if (match(TokenType.CONTINUE)) return continueStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())

        return expressionStatement()
    }

    /**
     * printStmt -> "print" expression ";" ;
     */
    private fun printStatement(): Stmt {
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect a ';' after value.")
        return Stmt.Print(value)
    }

    /**
     * exprStmt -> expression ';' ;
     */
    private fun expressionStatement(): Stmt {
        val expr: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect a ';' after value.")
        return Stmt.Expression(expr)
    }

    /**
     * block -> "{" declaration "}" ;
     */
    private fun block(): List<Stmt?> {
        val statements = ArrayList<Stmt?>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    /**
     * ifStmt -> "if" "(" expression ")" statement ( else statement )? ;
     */
    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition: Expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    /**
     * whileStmt -> "while" "(" expression ")" statement ;
     */
    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition: Expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        try {
            loopDepth++
            val body: Stmt = statement()

            return Stmt.While(condition, body)
        } finally {
            loopDepth--
        }
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        // initializer
        var initializer: Stmt? = null
        if (match(TokenType.SEMICOLON)) {
            val initializer: Stmt? = null
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration()
        } else {
            initializer = expressionStatement()
        }

        // condition
        var condition: Expr? = null
        if (!check(TokenType.SEMICOLON)) {
            condition = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        // increment
        var increment: Expr? = null
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression()
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clause.")

        try {
            loopDepth++

            var body: Stmt = statement()

            if (increment != null) {
                body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
            }

            if (condition == null) condition = Expr.Literal(true)
            body = Stmt.While(condition, body)

            if (initializer != null) {
                body = Stmt.Block(listOf(initializer, body))
            }

            return body
        } finally {
            loopDepth--
        }
    }

    /**
     * breakStmt -> ;
     */
    private fun breakStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Must be inside a loop to use a 'break'.")
        }
        consume(TokenType.SEMICOLON, "Expect ';' after break.")
        return Stmt.Break()
    }

    /**
     * continueStmt -> ;
     */
    private fun continueStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Must be inside a loop to use a 'continue'.")
        }
        consume(TokenType.SEMICOLON, "Expect ';' after continue.")
        return Stmt.Continue()
    }

    /**
     * expression -> comma ;
     */
    private fun expression(): Expr {
        return comma()
    }

    /**
     * comma -> conditional ( "," conditional )* ;
     */
    private fun comma(): Expr {
        val expressions = ArrayList<Expr>()
        expressions.add(conditional())

        while (match(TokenType.COMMA)) {
            expressions.add(conditional())
        }

        if (expressions.size == 1) {
            return expressions[0]
        }

        return Expr.Comma(expressions)
    }

    /**
     *  conditional -> assignment ( "?" expression ":" conditional )? ;
     *
     */
    private fun conditional(): Expr {
        var expr = assignment()

        if (match(TokenType.QUESTION)) {
            val thenBranch: Expr = expression()
            consume(TokenType.COLON, "Expect ':' after then branch of conditional expression.")
            val elseBranch: Expr = conditional()
            expr = Expr.Conditional(expr, thenBranch, elseBranch)
        }

        return expr
    }

    /**
     * assignment > IDENTIFIER '=' assignment | logic_or ;
     */
    private fun assignment(): Expr {
        val expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name: Token = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    /**
     * logic_or -> logic_and ( "or" logic_and )* ;
     */
    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator: Token = previous()
            val right: Expr = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    /**
     * logic_and -> equality ( "and" equality )* ;
     */
    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator: Token = previous()
            val right: Expr = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    /**
     * equality -> ( ( "!=" | "==" ) comparison )* ;
     */
    private fun equality(): Expr {
        var expr: Expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     */
    private fun comparison(): Expr {
        var expr: Expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * term -> factor ( ( "-" | "+" ) factor )* ;
     */
    private fun term(): Expr {
        var expr: Expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator: Token = previous()
            val right: Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * factor -> unary ( ( "/" | "*" ) unary )* ;
     */
    private fun factor(): Expr {
        var expr: Expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator: Token = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * unary -> ( "!" | "-" ) unary | primary ;
     */
    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator: Token = previous()
            val right: Expr = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NONE)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr: Expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) curr++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[curr]
    }

    private fun previous(): Token {
        return tokens[curr - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Ze.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS -> return
                TokenType.FUNCTION -> return
                TokenType.VAR -> return
                TokenType.FOR -> return
                TokenType.IF -> return
                TokenType.WHILE -> return
                TokenType.PRINT -> return
                TokenType.RETURN -> return
                else -> {}
            }

            advance()
        }
    }
}