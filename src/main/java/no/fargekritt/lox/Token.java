package no.fargekritt.lox;

public class Token {
    // Token types
    public final TokenType type;
    // Name of the token
    public final String lexeme;
    //
    public final Object literal;
    // Line in the source code
    public final int line;


    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString(){
        return type + " " + lexeme + " " + literal;
    }
}
