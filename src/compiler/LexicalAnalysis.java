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
                        tokens.add("/*");
                        i += 2;
                        while (i < text.length() - 1 && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
                            if (text.charAt(i) == '\n') {
                                currentLine++;
                            } else if (!Character.isWhitespace(text.charAt(i))) {
                                currentToken.append(text.charAt(i));
                            } else if (currentToken.length() > 0) {
                                tokens.add(currentToken.toString());
                                currentToken.setLength(0);
                            }
                            i++;
                        }
                        if (currentToken.length() > 0) {
                            tokens.add(currentToken.toString());
                            currentToken.setLength(0);
                        }
                        tokens.add("*/");
                        i += 2;
                        continue;
                    }
                }
            }

            // Manejo de operadores compuestos
            if (isOperatorChar(currentChar)) {
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
                continue;
            }

            // Manejo de separadores
            if (isSeparator(currentChar)) {
                if (currentToken.length() > 0) {
                    addToken(tokens, currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(currentChar));
                i++;
                continue;
            }

            // Construcción de identificadores y números
            if (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
                currentToken.append(currentChar);
                i++;
            } else {
                errors.add("Error léxico: carácter inválido '" + currentChar + "' en línea " + currentLine);
                i++;
            }
        }

        // Agregar el último token si existe
        if (currentToken.length() > 0) {
            addToken(tokens, currentToken.toString());
        }

        return errors.isEmpty() ? tokens : errors;
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