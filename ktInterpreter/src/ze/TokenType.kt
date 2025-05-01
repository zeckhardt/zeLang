package ze

enum class TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    QUESTION, COLON,

    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER_EQUAL,
    GREATER, LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUNCTION, FOR, IF,
    NONE, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    BREAK, CONTINUE,

    EOF
}