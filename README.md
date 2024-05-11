# crafting interpreters

http://www.craftinginterpreters.com

This project is the java part of the book.

## Chapter 2 - A map of the territory

---

### Chapter 2.1 The parts of a language

#### Lexer/Scanner

* Takes source code and turn it into tokens
    * var vari = a; turn into ["var", "veri", "=", "a", ";"]
    * ![lexer-token.png](lexer-token.png)
        * Removes stuff like whitespace and comments

#### Parsing

* Turns the list of tokens into an Abstract syntax tree or AST
* ![Parsing-AST.png](Parsing-AST.png)

#### Static analysis

* The First step in the analysis is to bind all the identifiers together.
    * (a + b) what is a and what is b, find their declaration and bind them together.
        * if the language is statically, this is where type checking happens
    * Multiple ways of storing the information gather during this process
        * Store the attributes in the AST itself
        * Lookup table/Symbol table
        * Transform the tree into a new structure that directly expresses the code.

#### intermediate representation

* an interface between the source and compiler, makes it easier to write one compiler and many "frontends"

#### Optimization

* Not the focus of this book
* Keywords for later research:
    * constant propagation
    * common subexpression elimination
    * loop invariant code motion
    * global value numbering
    * strength reduction
    * scalar replacement of aggregates
    * dead code elimination
    * loop unrolling

#### Code gen

* Real CPU or Virtual
    * Real need assembly, one compiler for each CPU architecture, ARM, x86
    * Virtual compiles to Bytecode (former P-code), instructions are often one byte long

#### Virtual machine

* two options for bytecode
    * mini-compiler, compiles the bytecode into native code. the bytecode becomes an IR
    * Virtual machine, to simulate a CPU that can run your bytecode in runtime.
        * If you write the VM in C you can compile the VM to different CPUs (C part of the book does this)

#### Runtime

* The runtime provides services to the language while it's running:
    * Garbage collection
    * instance of
* In fully complied languages the runtime is inserted in the executable (Go)
* If the language is interpreted or runs in a VM, the runtime is connected there (java)

### Chapter 2.2 Shortcuts and alternate routes

#### Single-pass compilers

* Compiles the code in a single pass.
    * Saves memory, don't need to store AST or an IR
* When C was made, it was a single-pass compiler, which is why you need declaration of a function before using it

#### Tree-walker interpreters

* Runs on the AST, in general, slow and often used for student projects and small languages.
* The first interpreter we make in this book works like this.

#### Transpiler

* Translating source code to a different language.
* If the languages are semantically similar, there might be no static analysis

#### Just-In-Time compilation

* Compiles to native code from bytecode (Java) or source (javascript) while running the code.
* Recompiles the code during execution of the code
  * Hooks to profile the program to optimize and recompile.

### Chapter 2.3 Compilers and interpreters

* Compiling is the act of translating source code to normally lower level code.
* Transpiling is compiling.
* A compiler compiles the code but doesn't run it
* An interpreter runs the source code, "runs from source"
* Many scripting languages are mix of both interpreted and compiled.
  * CPython, Lua is a couple of languages like this

