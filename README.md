# zeLang Programming Language
Implementation of both a Tree-Walk Interpreter traversing an Abstract Syntax Tree (AST) and a Bytecode Virtual Machine Interpreter. Both were developed using the guidance of the book
"Crafting Interpreters" by Robert Nystrom.

## About
**Tech Stack:** Kotlin, C, CMake
<br>

Two full language interpreters for the new language zeLang. zeLang is a dynamic typed scripting language. The language has full support for:
- Classes
- Functions
- Annonymous functions
- C-styled sytax for most components.
- Ternary operators
<br>

### **Tree-Walk Interpreter**
Kotlin was the chosen language for the implementation of it due to the close relationship with Java. Going further into it there a numerouse other benifits of Kotlin when implementing an interperter like this.
Kotlin has static typing just like Java but with the bonus of type inferencing, unlike the counterpart Java. The type casting is also very simplistic with the `as` keyword for casting and `is` keyword for checking.
The Visitor pattern is a common useage for an interpreter like this. For this implementation a much more elegant solution to this is utilizing a pseudo pattern matching with the `when` keyword. Kotlin is also a better
set of syntax over Java, keeping strengths but removing overly verbose components.
<br>

### **Bytecode Virtual Machine**
C was the chosen language for this implementation due to C's numerous strengths as a highly portable language, which is important for a Bytecode VM. There is also high performance without the need of an overly complex syntax. C also provides low-level access to memory management, unlike a lot of other languages. You can manage your own stack, heap, and memory without the abstractions and overhead of higher-level languages. Learning was another major driving factor for selecting C. Having the ability to learn low-level concepts and fundimentals I believe is key to becoming a great engineer.
<br>

## Language Documentation
