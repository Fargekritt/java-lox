package no.fargekritt.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(String name, Expr.Function declaration, Environment closure, boolean isInitializer) {
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public String toString() {
        if (name == null) return "<fn>";
        return "<fn "+ name+ ">";
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++) {
                environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }
        try{
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue){
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        if (isInitializer) return closure.getAt(0,"this");
        return null;
    }
    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction("this",declaration, environment, isInitializer);
    }
}
