// Parser.java

import java.util.ArrayList;

public class Parser {
    private ArrayList<String> tokens;
    private int currentPosition = 0;
    private int currentLine = 1;
    private String currentToken;
    private ArrayList<String> errors = new ArrayList<>();
    private ArrayList<String> declaredVariables = new ArrayList<>();

    public Parser(ArrayList<String> tokens) {
        this.tokens = tokens;
        if (!tokens.isEmpty()) {
            this.currentToken = tokens.get(0);
        }
    }

    public ArrayList<String> parse() {
        while (currentPosition < tokens.size()) {
            if (currentToken.equals("\n")) {
                currentLine++;
                advance();
                continue;
            }

            if (isDataType(currentToken)) {
                parseDeclaration();
            } else if (currentToken.equals("if")) {
                parseIfStatement();
            } else if (isIdentifier(currentToken)) {
                if (!isDeclaredVariable(currentToken)) {
                    addError("Variable no declarada: " + currentToken);
                }
                parseAssignment();
            } else {
                advance();
            }
        }
        return errors.isEmpty() ? new ArrayList<String>() {{ add("success"); }} : errors;
    }

    private void parseDeclaration() {
        String type = currentToken;
        advance();

        if (!isIdentifier(currentToken)) {
            addError("Se esperaba un identificador que comience con '_'");
            return;
        }

        String identifier = currentToken;
        if (isDeclaredVariable(identifier)) {
            addError("Variable ya declarada: " + identifier);
            return;
        }
        declaredVariables.add(identifier);
        advance();

        if (currentToken.equals("=")) {
            advance();
            parseExpression();
        }

        if (!currentToken.equals(";")) {
            addError("Se esperaba ';' al final de la declaración");
            return;
        }
        advance();
    }

    private void parseIfStatement() {
        advance(); // consume 'if'

        if (!currentToken.equals("(")) {
            addError("Se esperaba '(' después de 'if'");
            return;
        }
        advance();

        parseExpression();

        if (!currentToken.equals(")")) {
            addError("Se esperaba ')' en la declaración if");
            return;
        }
        advance();

        if (!currentToken.equals("then")) {
            addError("Se esperaba 'then' después de la condición if");
            return;
        }
        advance();

        if (!currentToken.equals("{")) {
            addError("Se esperaba '{' después de then");
            return;
        }
        advance();

        while (currentPosition < tokens.size() && !currentToken.equals("}")) {
            if (currentToken.equals("write")) {
                parseWriteStatement();
            } else if (isIdentifier(currentToken)) {
                if (!isDeclaredVariable(currentToken)) {
                    addError("Variable no declarada: " + currentToken);
                }
                parseAssignment();
            } else if (!currentToken.equals("\n")) {
                addError("Sentencia inválida dentro del bloque if");
                advance();
            } else {
                advance();
            }
        }

        if (!currentToken.equals("}")) {
            addError("Se esperaba '}' al final del bloque if");
        } else {
            advance();
        }
    }

    private void parseWriteStatement() {
        advance();

        if (!currentToken.equals("(")) {
            addError("Se esperaba '(' después de write");
            return;
        }
        advance();

        if (!isIdentifier(currentToken) && !isNumber(currentToken)) {
            addError("Se esperaba un identificador o número en la sentencia write");
            return;
        }

        if (isIdentifier(currentToken) && !isDeclaredVariable(currentToken)) {
            addError("Variable no declarada: " + currentToken);
        }
        advance();

        if (!currentToken.equals(")")) {
            addError("Se esperaba ')' después del argumento de write");
            return;
        }
        advance();

        if (!currentToken.equals(";")) {
            addError("Se esperaba ';' después de la sentencia write");
            return;
        }
        advance();
    }

    private void parseExpression() {
        parseTerm();
        while (isArithmeticOperator(currentToken) || isRelationalOperator(currentToken)) {
            advance();
            parseTerm();
        }
    }

    private void parseTerm() {
        if (isIdentifier(currentToken)) {
            if (!isDeclaredVariable(currentToken)) {
                addError("Variable no declarada: " + currentToken);
            }
            advance();
        } else if (isNumber(currentToken)) {
            advance();
        } else if (currentToken.equals("(")) {
            advance();
            parseExpression();
            if (!currentToken.equals(")")) {
                addError("Falta paréntesis de cierre");
            } else {
                advance();
            }
        } else {
            addError("Se esperaba un identificador o número");
        }
    }

    private boolean isDataType(String token) {
        return token.equals("long") || token.equals("double");
    }

    private boolean isIdentifier(String token) {
        return token != null && token.startsWith("_") &&
                token.length() > 1 &&
                token.substring(1).matches("[a-zA-Z0-9_]+");
    }

    private boolean isDeclaredVariable(String identifier) {
        return declaredVariables.contains(identifier);
    }

    private boolean isArithmeticOperator(String token) {
        return token.equals("+") || token.equals("-") ||
                token.equals("*") || token.equals("/");
    }

    private boolean isRelationalOperator(String token) {
        return token.equals(">") || token.equals("<") ||
                token.equals(">=") || token.equals("<=") ||
                token.equals("==") || token.equals("!=");
    }

    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void parseAssignment() {
        String identifier = currentToken;
        advance();

        if (!currentToken.equals("=")) {
            addError("Se esperaba '=' en la asignación");
            return;
        }
        advance();

        parseExpression();

        if (!currentToken.equals(";")) {
            addError("Se esperaba ';' al final de la asignación");
            return;
        }
        advance();
    }

    private void advance() {
        currentPosition++;
        if (currentPosition < tokens.size()) {
            currentToken = tokens.get(currentPosition);
        }
    }

    private void addError(String message) {
        errors.add("Error: " + message + " en línea " + currentLine);
    }
}