package no.fargekritt.lox;

import java.util.Objects;

public class Variable {
    Token name;
    int usage;

    public Variable(Token name) {
        this.name = name;
        usage = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable variable)) return false;
        return Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
