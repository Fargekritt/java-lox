package no.fargekritt.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableManager {
    private final Map<Variable, Boolean> scope = new HashMap<>();
    private final Map<String, Variable> variableNameMap = new HashMap<>();

    public void declare(Token name){
        if (variableNameMap.containsKey(name.lexeme)){
            Lox.error(name, "Already a variable with this name in the scope");
        }

        Variable variable = new Variable(name);

        scope.put(variable, false);
        variableNameMap.put(name.lexeme, variable);
    }

    public void define(Token name){
        scope.put(variableNameMap.get(name.lexeme), true);
    }

    public boolean containsVar(String lexeme) {
        return variableNameMap.containsKey(lexeme);
    }

    public Boolean varReady(String lexeme) {
        return scope.get(variableNameMap.get(lexeme));
    }

    public void addUsage(String lexeme) {
        variableNameMap.get(lexeme).usage++;
    }

    public List<Variable> getUnusedVars(){
        List<Variable> unusedVars = new ArrayList<>();
        for (Map.Entry<Variable, Boolean> variable : scope.entrySet()) {
            if(variable.getKey().usage == 0 && variable.getValue()){
                unusedVars.add(variable.getKey());
            }
        }
        return unusedVars;
    }
}
