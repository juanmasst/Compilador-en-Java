// Readfiles.java
package compiler;

import java.io.*;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Readfiles {
    private ArrayList<String> tokens = new ArrayList<>();

    public ArrayList<String> readfile(File file) throws IOException {
        String content = readFileContent(file);
        LexicalAnalysis lexicalAnalyzer = new LexicalAnalysis(content);
        tokens = lexicalAnalyzer.scan();
        return tokens;
    }

    // Método estático para leer el contenido del archivo
    public static String readFileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }
}