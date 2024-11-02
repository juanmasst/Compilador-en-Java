package compiler;

import java.util.ArrayList;

public class LexicalAnalysis {
    private String text;
    private ArrayList<String> errors;
    private int currentLine;

    public LexicalAnalysis(String text) {
        this.text = text;
        this.errors = new ArrayList<>();
        this.currentLine = 1;
    }

    public ArrayList<String> scan() {
        ArrayList<String> tokens = new ArrayList<>();
        int i = 0;
        StringBuilder currentToken = new StringBuilder();

        while (i < text.length()) {
            char currentChar = text.charAt(i);

            // Ignorar todos los caracteres de espacio en blanco (incluyendo saltos de línea)
            if (Character.isWhitespace(currentChar)) {
                if (currentChar == '\n') {
                    currentLine++;
                }
                if (currentToken.length() > 0) {
                    addToken(tokens, currentToken.toString());
                    currentToken.setLength(0);
                }
                i++;
                continue;
            }

            // Manejo de comentarios
            if (currentChar == '/') {
                if (i + 1 < text.length()) {
                    if (text.charAt(i + 1) == '/') {
                        // Comentario de línea simple
                        while (i < text.length() && text.charAt(i) != '\n') {
                            i++;
                        }
                        continue;
                    } else if (text.charAt(i + 1) == '*') {
                        // Comentario multilínea
                        i += 2;
                        while (i < text.length() - 1 && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
                            if (text.charAt(i) == '\n') {
                                currentLine++;
                            }
                            i++;
                        }
                        i += 2;
                        continue;
                    }
                }
            }

            // Manejo de números (incluyendo decimales)
            if (Character.isDigit(currentChar)) {
                currentToken.append(currentChar);
                i++;
                continue;
            }
            
            // Detectar y rechazar comas en números
            if (currentChar == ',' && currentToken.length() > 0 && Character.isDigit(currentToken.charAt(currentToken.length() - 1))) {
                errors.clear();
                errors.add("Error: Use punto (.) en lugar de coma (,) en línea " + currentLine);
                return errors;
            }

            // Manejo del punto decimal
            if (currentChar == '.' && currentToken.length() > 0 && Character.isDigit(currentToken.charAt(currentToken.length() - 1))) {
                currentToken.append(currentChar);
                i++;
                continue;
            }

            // Construcción de identificadores
            if (Character.isLetter(currentChar) || currentChar == '_') {
                currentToken.append(currentChar);
                i++;
            } else if (!Character.isDigit(currentChar)) {  // Si no es dígito ni letra ni _
                if (currentToken.length() > 0) {
                    addToken(tokens, currentToken.toString());
                    currentToken.setLength(0);
                }
                
                // Si es un operador o separador, procesarlo
                if (isOperatorChar(currentChar) || isSeparator(currentChar)) {
                    if (currentToken.length() > 0) {
                        addToken(tokens, currentToken.toString());
                        currentToken.setLength(0);
                    }
                    
                    String operator = String.valueOf(currentChar);
                    if (i + 1 < text.length() && isOperatorChar(text.charAt(i + 1))) {
                        operator += text.charAt(i + 1);
                        i++;
                    }
                    tokens.add(operator);
                    i++;
                } else {
                    errors.clear();
                    errors.add("Error: Carácter inválido '" + currentChar + "' en línea " + currentLine);
                    return errors;
                }
            } else {
                currentToken.append(currentChar);
                i++;
            }
        }

        // Agregar el último token si existe
        if (currentToken.length() > 0) {
            addToken(tokens, currentToken.toString());
        }

        // Retornar errores si existen, o tokens con "success" si no hay errores
        if (!errors.isEmpty()) {
            return errors;
        } else {
            ArrayList<String> successTokens = new ArrayList<>();
            successTokens.add("\nsuccess");
            successTokens.addAll(tokens);
            return successTokens;
        }
    }

    private void addToken(ArrayList<String> tokens, String token) {
        if (!token.trim().isEmpty()) {  // Solo agregar tokens no vacíos
            tokens.add(token);
        }
    }

    private boolean isOperatorChar(char c) {
        return "+-*/><=!".indexOf(c) != -1;
    }

    private boolean isSeparator(char c) {
        return "(){};,".indexOf(c) != -1;
    }
}