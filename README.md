# zeLang

## Grammar rules:

`program        -> statement* EOF ;`

`statement      -> exprStmt | printStmt | ifStmt ;`

`exprStmt       -> expression ';' ;`

`printStmt      -> "print" expression ";" ;`

`ifStmt         -> "if" "(" expression ")" statement ( else statement )? ;`

`expression     -> equality ;`

`equality       -> ( ( "!=" | "==" ) comparison )* ;`

`comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;`

`term           -> factor ( ( "-" | "+" ) factor )* ;`

`factor         -> unary ( ( "/" | "*" ) unary )* ;`

`unary          -> ( "!" | "-" ) unary | primary ;`

`primary        -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;`