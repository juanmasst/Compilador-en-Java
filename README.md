# Compilador-en-Java
Este es un compilador en Java para la materia de Compiladores 2024

## Compilación y Ejecución

Para compilar y ejecutar el compilador, sigue estos pasos:

1. Crear el directorio bin:
mkdir bin

2. Compilar los archivos fuente:

```bash
javac -d bin src/compiler/*.java
```

3. Crear el archivo MANIFEST.MF:

text:MANIFEST.MF
Manifest-Version: 1.0
Created-By: 1.0
Main-Class: compiler.Compiler
file.encoding: UTF-8


Nota: Asegúrate de incluir una línea en blanco al final del archivo.

4. Crear el archivo JAR:
```bash
jar cfm Compiler.jar MANIFEST.MF -C bin .
```

5. Ejecutar el compilador:

```bash
java -jar Compiler.jar ./test
```

El programa procesará todos los archivos .txt en el directorio especificado y mostrará:
- El contenido de cada archivo
- Los resultados del análisis léxico
- Los resultados del análisis sintáctico