//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package compiler;

import java.util.ArrayList;

public class Parser {
    private ArrayList<String> tokens;
    private int currentPosition = 0;
    private int currentLine = 1;
    private String currentToken;
    private ArrayList<String> errors = new ArrayList<>();
    private ArrayList<String> declaredVariables = new ArrayList<>();

    public Parser(ArrayList<String> var1) {
        this.tokens = var1;
        if (!var1.isEmpty()) {
            this.currentToken = (String)var1.get(0);
        }

    }

    public ArrayList<String> parse() {
        while(this.currentPosition < this.tokens.size()) {
            if (this.currentToken.equals("\n")) {
                ++this.currentLine;
                this.advance();
            } else if (this.isDataType(this.currentToken)) {
                this.parseDeclaration();
            } else if (this.currentToken.equals("if")) {
                this.parseIfStatement();
            } else if (this.isIdentifier(this.currentToken)) {
                if (!this.isDeclaredVariable(this.currentToken)) {
                    this.addError("Variable no declarada: " + this.currentToken);
                }

                this.parseAssignment();
            } else {
                this.advance();
            }
        }

        return this.errors.isEmpty() ? new ArrayList<String>() {
            {
                this.add("success");
            }
        } : this.errors;
    }

    private void parseDeclaration() {
        String var1 = this.currentToken;
        this.advance();
        if (!this.isIdentifier(this.currentToken)) {
            this.addError("Se esperaba un identificador que comience con '_'");
        } else {
            String var2 = this.currentToken;
            if (this.isDeclaredVariable(var2)) {
                this.addError("Variable ya declarada: " + var2);
            } else {
                this.declaredVariables.add(var2);
                this.advance();
                if (this.currentToken.equals("=")) {
                    this.advance();
                    this.parseExpression();
                }

                if (!this.currentToken.equals(";")) {
                    this.addError("Se esperaba ';' al final de la declaración");
                } else {
                    this.advance();
                }
            }
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
        
        if (this.currentToken.equals("else")) {
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
            } else if (!this.currentToken.equals("\n")) {
                this.addError("Sentencia inválida dentro del bloque");
                this.advance();
            } else {
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
        } else {
            this.advance();
            if (!this.isIdentifier(this.currentToken) && !this.isNumber(this.currentToken)) {
                this.addError("Se esperaba un identificador o número en la sentencia write");
            } else {
                if (this.isIdentifier(this.currentToken) && !this.isDeclaredVariable(this.currentToken)) {
                    this.addError("Variable no declarada: " + this.currentToken);
                }

                this.advance();
                if (!this.currentToken.equals(")")) {
                    this.addError("Se esperaba ')' después del argumento de write");
                } else {
                    this.advance();
                    if (!this.currentToken.equals(";")) {
                        this.addError("Se esperaba ';' después de la sentencia write");
                    } else {
                        this.advance();
                    }
                }
            }
        }
    }

    private void parseExpression() {
        this.parseTerm();

        while(this.isArithmeticOperator(this.currentToken) || this.isRelationalOperator(this.currentToken)) {
            this.advance();
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
            if (!this.currentToken.equals(")")) {
                this.addError("Falta paréntesis de cierre");
            } else {
                this.advance();
            }
        } else {
            this.addError("Se esperaba un identificador o número");
        }

    }

    private boolean isDataType(String var1) {
        return var1.equals("long") || var1.equals("double");
    }

    private boolean isIdentifier(String var1) {
        return var1 != null && var1.startsWith("_") && var1.length() > 1 && var1.substring(1).matches("[a-zA-Z0-9_]+");
    }

    private boolean isDeclaredVariable(String var1) {
        return this.declaredVariables.contains(var1);
    }

    private boolean isArithmeticOperator(String var1) {
        return var1.equals("+") || var1.equals("-") || var1.equals("*") || var1.equals("/");
    }

    private boolean isRelationalOperator(String var1) {
        return var1.equals(">") || var1.equals("<") || var1.equals(">=") || 
               var1.equals("<=") || var1.equals("==") || var1.equals("!=") || 
               var1.equals("<>");
    }

    private boolean isNumber(String var1) {
        try {
            Double.parseDouble(var1);
            return true;
        } catch (NumberFormatException var3) {
            return false;
        }
    }

    private void parseAssignment() {
        String var1 = this.currentToken;
        this.advance();
        if (!this.currentToken.equals("=")) {
            this.addError("Se esperaba '=' en la asignación");
        } else {
            this.advance();
            this.parseExpression();
            if (!this.currentToken.equals(";")) {
                this.addError("Se esperaba ';' al final de la asignación");
            } else {
                this.advance();
            }
        }
    }

    private void advance() {
        ++this.currentPosition;
        if (this.currentPosition < this.tokens.size()) {
            this.currentToken = (String)this.tokens.get(this.currentPosition);
        }

    }

    private void addError(String var1) {
        this.errors.add("Error: " + var1 + " en línea " + this.currentLine);
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
}
