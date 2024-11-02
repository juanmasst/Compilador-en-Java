package compiler;

import java.util.ArrayList;

public class Parser {
    private ArrayList<String> tokens;
    private int currentPosition = 0;
    private int currentLine = 1;
    private String currentToken;
    private ArrayList<String> errors = new ArrayList<>();
    private ArrayList<String> declaredVariables = new ArrayList<>();

    // Banderas de debug
    private boolean DEBUG = true;
    private void debug(String method, String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + method + ": " + message + 
                             " | Token actual: " + this.currentToken + 
                             " | Línea: " + this.currentLine +
                             " | Pos: " + this.currentPosition);
        }
    }

    public Parser(ArrayList<String> tokens) {
        this.tokens = new ArrayList<>();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                this.tokens.add(token);
            }
        }
        this.currentPosition = 0;
        this.currentToken = this.tokens.get(0);
    }

    public ArrayList<String> parse() {
        debug("parse", "Iniciando análisis sintáctico");
        while (this.currentPosition < this.tokens.size()) {
            try {
                debug("parse", "Procesando siguiente token");
                
                // Primero procesamos los tipos de datos
                if (this.isDataType(this.currentToken)) {
                    debug("parse", "Procesando declaración");
                    this.parseDeclaration();
                } 
                // Luego las estructuras de control
                else if (this.currentToken.equals("if")) {
                    debug("parse", "Procesando if");
                    this.parseIfStatement();
                } else if (this.currentToken.equals("while")) {
                    debug("parse", "Procesando while");
                    this.parseWhileStatement();
                } else if (this.currentToken.equals("read")) {
                    debug("parse", "Procesando read");
                    this.parseReadStatement();
                } else if (this.currentToken.equals("write")) {
                    debug("parse", "Procesando write");
                    this.parseWriteStatement();
                }
                // Finalmente las asignaciones
                else if (this.isIdentifier(this.currentToken)) {
                    debug("parse", "Procesando asignación");
                    if (!this.isDeclaredVariable(this.currentToken)) {
                        this.addError("Variable no declarada: " + this.currentToken);
                    }
                    this.parseAssignment();
                } else {
                    this.advance();
                }
            } catch (Exception e) {
                debug("parse", "Error inesperado: " + e.getMessage());
                this.addError("Error inesperado procesando token: " + this.currentToken);
                this.advance();
            }
        }
        debug("parse", "Análisis sintáctico finalizado");
        return this.errors;
    }

    private void parseDeclaration() {
        String dataType = this.currentToken;
        this.advance();
        
        if (!this.isIdentifier(this.currentToken)) {
            this.addError("Se esperaba un identificador que comience con '_'");
            return;
        }
        
        String identifier = this.currentToken;
        if (this.isDeclaredVariable(identifier)) {
            this.addError("Variable ya declarada: " + identifier);
            return;
        }
        
        this.declaredVariables.add(identifier);
        this.advance();
        
        if (this.currentPosition >= this.tokens.size()) {
            this.addError("Fin inesperado en la declaración");
            return;
        }
        
        if (this.currentToken.equals("=")) {
            this.advance();
            if (dataType.equals("long") && this.currentToken.contains(".")) {
                this.addError("No se puede asignar un decimal a una variable long");
                return;
            }
            this.parseExpression();
        }
        
        if (this.currentPosition >= this.tokens.size() || !this.currentToken.equals(";")) {
            this.addError("Se esperaba ';' al final de la declaración");
        } else {
            this.advance();
        }
    }

    private void parseIfStatement() {
        this.advance();
        if (!this.currentToken.equals("(")) {
            this.addError("Se esperaba '(' después de 'if'");
            return;
        }
        
        this.advance();
        this.parseExpression();
        
        if (!this.currentToken.equals(")")) {
            this.addError("Se esperaba ')' en la declaración if");
            return;
        }
        
        this.advance();
        if (!this.currentToken.equals("then")) {
            this.addError("Se esperaba 'then' después de la condición if");
            return;
        }
        
        this.advance();
        this.parseBlock();
        
        if (this.currentPosition < this.tokens.size() && this.currentToken.equals("else")) {
            this.advance();
            this.parseBlock();
        }
    }

    private void parseBlock() {
        if (!this.currentToken.equals("{")) {
            this.addError("Se esperaba '{'");
            return;
        }
        
        this.advance();
        while (this.currentPosition < this.tokens.size() && !this.currentToken.equals("}")) {
            if (this.currentToken.equals("write")) {
                this.parseWriteStatement();
            } else if (this.currentToken.equals("read")) {
                this.parseReadStatement();
            } else if (this.currentToken.equals("while")) {
                this.parseWhileStatement();
            } else if (this.currentToken.equals("break")) {
                this.parseBreakStatement();
            } else if (this.isIdentifier(this.currentToken)) {
                if (!this.isDeclaredVariable(this.currentToken)) {
                    this.addError("Variable no declarada: " + this.currentToken);
                }
                this.parseAssignment();
            } else if (!this.currentToken.equals("\n") && !this.currentToken.equals(" ")) {
                this.advance();
            }
        }
        
        if (!this.currentToken.equals("}")) {
            this.addError("Se esperaba '}'");
            return;
        }
        this.advance();
    }

    private void parseWriteStatement() {
        this.advance();
        if (!this.currentToken.equals("(")) {
            this.addError("Se esperaba '(' después de write");
            return;
        }
        
        this.advance();
        if (!this.isIdentifier(this.currentToken) && !this.isNumber(this.currentToken)) {
            this.addError("Se esperaba un identificador o número en la sentencia write");
            return;
        }
        
        if (this.isIdentifier(this.currentToken) && !this.isDeclaredVariable(this.currentToken)) {
            this.addError("Variable no declarada: " + this.currentToken);
            return;
        }

        this.advance();
        if (!this.currentToken.equals(")")) {
            this.addError("Se esperaba ')' después del argumento de write");
            return;
        }
        
        this.advance();
        if (!this.currentToken.equals(";")) {
            this.addError("Se esperaba ';' después de la sentencia write");
            return;
        }
        
        this.advance();
    }

    private void parseExpression() {
        this.parseTerm();
        while (this.currentPosition < this.tokens.size() && 
               (this.isArithmeticOperator(this.currentToken) || 
                this.isRelationalOperator(this.currentToken))) {
            this.advance();
            if (this.currentPosition >= this.tokens.size()) {
                this.addError("Expresión incompleta");
                return;
            }
            this.parseTerm();
        }
    }

    private void parseTerm() {
        if (this.isIdentifier(this.currentToken)) {
            if (!this.isDeclaredVariable(this.currentToken)) {
                this.addError("Variable no declarada: " + this.currentToken);
            }
            this.advance();
        } else if (this.isNumber(this.currentToken)) {
            this.advance();
        } else if (this.currentToken.equals("(")) {
            this.advance();
            this.parseExpression();
            if (this.currentPosition >= this.tokens.size() || !this.currentToken.equals(")")) {
                this.addError("Falta paréntesis de cierre");
            } else {
                this.advance();
            }
        } else {
            this.addError("Se esperaba un identificador o número");
        }
    }

    private void parseAssignment() {
        debug("parseAssignment", "Iniciando");
        String identifier = this.currentToken;
        this.advance();
        
        if (this.currentPosition >= this.tokens.size()) {
            debug("parseAssignment", "Fin inesperado después de identificador");
            this.addError("Se esperaba '=' después del identificador " + identifier);
            return;
        }
        
        if (this.isArithmeticOperator(this.currentToken) || 
            this.isRelationalOperator(this.currentToken) ||
            this.currentToken.equals(")") ||
            this.currentToken.equals(";")) {
            return;
        }
        
        if (!this.currentToken.equals("=")) {
            debug("parseAssignment", "Token inesperado, se esperaba '='");
            this.addError("Se esperaba '=' después del identificador " + identifier);
            return;
        }
        
        debug("parseAssignment", "Procesando expresión después de =");
        this.advance();
        this.parseExpression();
        
        if (this.currentPosition >= this.tokens.size() || !this.currentToken.equals(";")) {
            debug("parseAssignment", "Falta punto y coma al final");
            this.addError("Se esperaba ';' al final de la asignación");
        } else {
            this.advance();
        }
        debug("parseAssignment", "Finalizado");
    }

    private void parseWhileStatement() {
        this.advance();
        if (!this.currentToken.equals("(")) {
            this.addError("Se esperaba '(' después de while");
            return;
        }
        
        this.advance();
        this.parseExpression();
        
        if (!this.currentToken.equals(")")) {
            this.addError("Se esperaba ')'");
            return;
        }
        
        this.advance();
        this.parseBlock();
    }

    private void parseBreakStatement() {
        this.advance();
        if (!this.currentToken.equals(";")) {
            this.addError("Se esperaba ';' después de break");
            return;
        }
        this.advance();
    }

    private void parseReadStatement() {
        this.advance();
        if (!this.currentToken.equals("(")) {
            this.addError("Se esperaba '(' después de read");
            return;
        }
        
        this.advance();
        if (!this.isIdentifier(this.currentToken)) {
            this.addError("Se esperaba un identificador");
            return;
        }
        
        if (!this.isDeclaredVariable(this.currentToken)) {
            this.addError("Variable no declarada: " + this.currentToken);
            return;
        }
        
        this.advance();
        if (!this.currentToken.equals(")")) {
            this.addError("Se esperaba ')'");
            return;
        }
        
        this.advance();
        if (!this.currentToken.equals(";")) {
            this.addError("Se esperaba ';'");
            return;
        }
        this.advance();
    }

    private boolean isDataType(String token) {
        return token.equals("long") || token.equals("double");
    }

    private boolean isIdentifier(String token) {
        return token != null && token.startsWith("_") && token.length() > 1 && 
               token.substring(1).matches("[a-zA-Z0-9_]+");
    }

    private boolean isDeclaredVariable(String token) {
        return this.declaredVariables.contains(token);
    }

    private boolean isArithmeticOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private boolean isRelationalOperator(String token) {
        return token.equals(">") || token.equals("<") || token.equals(">=") || 
               token.equals("<=") || token.equals("==") || token.equals("!=") || 
               token.equals("<>");
    }

    private boolean isNumber(String token) {
        try {
            if (token.contains(".")) {
                Double.parseDouble(token);
            } else {
                Long.parseLong(token);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void advance() {
        ++this.currentPosition;
        if (this.currentPosition < this.tokens.size()) {
            this.currentToken = this.tokens.get(this.currentPosition);
            if (this.currentToken.equals("\n")) {
                ++this.currentLine;
            }
        }
    }

    private void addError(String message) {
        String errorMsg = "Error: " + message + " en línea " + currentLine;
        if (!errors.contains(errorMsg)) {  // Prevención de errores duplicados
            errors.add(errorMsg);
        }
    }
}