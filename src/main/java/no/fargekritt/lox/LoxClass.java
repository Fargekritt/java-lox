package no.fargekritt.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    public final String name;
    private final Map<String, LoxFunction> methods;

    @Override
    public String toString() {
        return name;
    }

    public LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public LoxFunction findMethod(String lexeme) {
        return methods.getOrDefault(lexeme, null);
    }

}
