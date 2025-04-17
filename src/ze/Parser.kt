package ze

class Parser(
    private val tokens: List<Token>
) {
    private var curr: Int = 0
    private class ParseError: RuntimeException()

    fun parse(): Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    /**
     * expression -> equality ;
     */
    private fun expression(): Expr {
        return equality()
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