# zeLang

## Grammar rules:

`program        -> declaration* EOF ;`

`declaration    -> varDecl | statement ;`

`varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;`

`statement      -> exprStmt | printStmt | block | ifStmt ;`

`exprStmt       -> expression ';' ;`

`printStmt      -> "print" expression ";" ;`

`block          -> "{" declaration "}" ;`

`ifStmt         -> "if" "(" expression ")" statement ( else statement )? ;`

`expression     -> comma ;`

`comma          -> assignment ( "," assignment )* ;`

`assignment     -> IDENTIFIER '=' assignment | equality ;`

`equality       -> ( ( "!=" | "==" ) comparison )* ;`

`comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;`

`term           -> factor ( ( "-" | "+" ) factor )* ;`

`factor         -> unary ( ( "/" | "*" ) unary )* ;`

`unary          -> ( "!" | "-" ) unary | primary ;`

`primary        -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;`