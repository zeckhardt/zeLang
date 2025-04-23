# zeLang

## Grammar rules:

`program        -> declaration* EOF ;`

`declaration    -> varDecl | statement ;`

`varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;`

`statement      -> exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | breakStmt | continueStmt ;`

`exprStmt       -> expression ';' ;`

`printStmt      -> "print" expression ";" ;`

`block          -> "{" declaration "}" ;`

`ifStmt         -> "if" "(" expression ")" statement ( else statement )? ;`

`whileStmt      -> "while" "(" expression ")" statement ;`

`forStmt        -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;`

`breakStmt      -> ;`

`continueStmt   -> ;`

`expression     -> comma ;`

`comma          -> conditional ( "," conditional )* ;`

`conditional    -> assignment ( "?" expression ":" conditional )? ;`

`assignment     -> IDENTIFIER '=' assignment | logic_or ;`

`logic_or       -> logic_and ( "or" logic_and )* ;`

`logic_and      -> equality ( "and" equality )* ;`

`equality       -> ( ( "!=" | "==" ) comparison )* ;`

`comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;`

`term           -> factor ( ( "-" | "+" ) factor )* ;`

`factor         -> unary ( ( "/" | "*" ) unary )* ;`

`unary          -> ( "!" | "-" ) unary | primary ;`

`primary        -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;`