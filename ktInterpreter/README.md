# zeLang

## Grammar rules:

`program        -> declaration* EOF ;`

`declaration    -> varDecl | fnDecl | classDecl | statement ;`

`varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;`

`fnDecl         -> "fn" function ;`

`classDecl      -> "class" IDENTIFIER "{" function* "}" ;`

`function       -> IDENTIFIER "(" parameters? ")" block ;`

`parameters     -> IDENTIFIER ( "," IDENTIFIER )* ;`

`statement      -> exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | breakStmt | continueStmt | returnStmt ;`

`exprStmt       -> expression ';' ;`

`printStmt      -> "print" expression ";" ;`

`block          -> "{" declaration "}" ;`

`ifStmt         -> "if" "(" expression ")" statement ( else statement )? ;`

`whileStmt      -> "while" "(" expression ")" statement ;`

`forStmt        -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;`

`breakStmt      -> ;`

`continueStmt   -> ;`

`returnStmt     -> "return" expression? ";" ;`

`expression     -> comma ;`

`comma          -> conditional ( "," conditional )* ;`

`conditional    -> assignment ( "?" expression ":" conditional )? ;`

`assignment     -> ( call "." )? IDENTIFIER "=" assignment | logic_or ;`

`logic_or       -> logic_and ( "or" logic_and )* ;`

`logic_and      -> equality ( "and" equality )* ;`

`equality       -> ( ( "!=" | "==" ) comparison )* ;`

`comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;`

`term           -> factor ( ( "-" | "+" ) factor )* ;`

`factor         -> unary ( ( "/" | "*" ) unary )* ;`

`unary          -> ( "!" | "-" ) unary | call ;`

`call           -> primary ( "(" arguments? ")" | "." IDENTIFIER )* ;`

`primary        -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;`