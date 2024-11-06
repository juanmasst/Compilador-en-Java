// Compiler.java
package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        try {
            // Verificar si se proporcionó un directorio como argumento
            if (args.length == 0) {
                System.out.println("Error: Please provide a test directory path");
                return;
            }

            File directory = new File(args[0]);
            if (!directory.isDirectory()) {
                System.out.println("Error: The path provided is not a directory - " + args[0]);
                return;
            }

            // Obtener todos los archivos .txt del directorio
            File[] testFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

            if (testFiles == null || testFiles.length == 0) {
                System.out.println("No test files (.txt) found in directory: " + args[0]);
                return;
            }

            // Procesar cada archivo
            for (File testFile : testFiles) {
                processTestFile(testFile);
            }

        } catch (Exception e) {
            System.err.println("Error during compilation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processTestFile(File file) {
        System.out.println("\n========================================");
        System.out.println("Processing file: " + file.getName());
        System.out.println("========================================");

        try {
            String fileContent = Readfiles.readFileContent(file);
            System.out.println("\nFile content:");
            System.out.println("----------------------------------------");
            System.out.println(fileContent);
            System.out.println("----------------------------------------");

            LexicalAnalysis lexer = new LexicalAnalysis(fileContent);
            ArrayList<String> tokens = lexer.scan();

            if (!tokens.isEmpty() && tokens.get(0).startsWith("Error:")) {
                System.out.println("\nErrores léxicos encontrados:");
                System.out.println("----------------------------------------");
                System.out.println(tokens.get(0));
                return;
            }

            System.out.println("\nAnálisis léxico completado exitosamente.");

            System.out.println("\nResultados del análisis sintáctico:");
            System.out.println("----------------------------------------");
            ArrayList<String> tokensForParser = new ArrayList<>(tokens.subList(1, tokens.size()));
            Parser parser = new Parser(tokensForParser);
            ArrayList<String> results = parser.parse();

            if (results.isEmpty()) {
                System.out.println("Análisis sintáctico completado exitosamente.");
            } else {
                for (String result : results) {
                    System.out.println(result);
                }
            }

        } catch (IOException e) {
            System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
        }
    }
}