package at.jku.dke.task_app.jdbc.evaluation;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom implementation of 'JavaFileObject' that represents a file in memory.
 * This class is used for storing Java source code as well as bytecode during
 * the compilation process in memory.
 */
public class MemoryJavaFile extends SimpleJavaFileObject {

    // The source code as a string (used when the file represents source code)
    private final String sourceCode;

    // The byte output stream used to store the compiled bytecode (used when the file represents compiled classes)
    private final ByteArrayOutputStream outputStream;

    // The kind of the file (either source code or bytecode)
    private final Kind kind;

    /**
     * Constructor for source code representation.
     * This constructor is used when the file represents Java source code.
     *
     * @param className  The name of the class
     * @param sourceCode The Java source code to be compiled
     */
    public MemoryJavaFile(String className, String sourceCode) {
        // Create a unique URI for this file
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;  // Store the source code
        this.outputStream = null;  // No output stream needed for source code
        this.kind = Kind.SOURCE;  // Mark this file as source code
    }

    /**
     * Constructor for bytecode representation.
     * This constructor is used when the file represents compiled bytecode.
     *
     * @param className The fully qualified name of the class
     * @param kind      The type of file (either source or bytecode)
     */
    public MemoryJavaFile(String className, Kind kind) {
        // Create a unique URI for this file
        super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
        this.outputStream = new ByteArrayOutputStream();  // Initialize the output stream for bytecode
        this.sourceCode = null;  // No source code for bytecode representation
        this.kind = kind;  // Mark the file type
    }

    /**
     * Returns the source code content for the file.
     * This method is only supported for source code files and throws an exception for bytecode files.
     *
     * @param ignoreEncodingErrors Ignored in this implementation
     * @return The source code as a string
     * @throws UnsupportedOperationException If called on a bytecode file
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        if (kind == Kind.SOURCE) return sourceCode;  // Return source code if it's a source file
        throw new UnsupportedOperationException("getCharContent() not supported for compiled class files");
    }

    /**
     * Returns an output stream to write the compiled bytecode.
     * This method is only supported for bytecode files and throws an exception for source code files.
     *
     * @return The output stream for bytecode
     * @throws IllegalStateException If called on a source file
     */
    @Override
    public OutputStream openOutputStream() {
        if (outputStream == null) throw new IllegalStateException("Output stream is not available for source files.");
        return outputStream;  // Return the byte output stream if it's a bytecode file
    }

    /**
     * Returns the compiled bytecode as a byte array.
     * This method is only meaningful for bytecode files.
     *
     * @return The bytecode as a byte array, or null if it's a source file
     */
    public byte[] getCompiledBytes() {
        return outputStream != null ? outputStream.toByteArray() : null;  // Return bytecode if available
    }

    /**
     * Helper class to manage the in-memory Java file objects during compilation.
     * This extends 'ForwardingJavaFileManager' to intercept and store compiled class files in memory.
     */
    public static class FileManagerMap extends javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager> {

        // A map to store compiled class files (key: class name, value: 'MemoryJavaFile' representing bytecode)
        private final Map<String, MemoryJavaFile> compiledClasses = new HashMap<>();

        /**
         * Constructor that accepts a standard Java file manager.
         *
         * @param fileManager The standard Java file manager to forward calls to when necessary
         */
        public FileManagerMap(javax.tools.JavaFileManager fileManager) {
            super(fileManager);
        }

        /**
         * Intercepts requests for output Java files during compilation and stores them in memory.
         *
         * @param location The location of the file (not used in this implementation)
         * @param className The name of the class being compiled
         * @param kind The type of file (source or bytecode)
         * @param sibling Not used in this implementation
         * @return The 'MemoryJavaFile' to store the class's bytecode in memory
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, javax.tools.FileObject sibling) {
            MemoryJavaFile file = new MemoryJavaFile(className, kind);  // Create a new 'MemoryJavaFile'
            compiledClasses.put(className, file);  // Store it in the map by class name
            return file;  // Return the memory file object for the compiled class
        }

        /**
         * Returns a map of all compiled classes with their bytecode.
         *
         * @return A map where the key is the class name and the value is the bytecode as a byte array
         */
        public Map<String, byte[]> getAllClassBytes() {
            Map<String, byte[]> byteMap = new HashMap<>();
            for (Map.Entry<String, MemoryJavaFile> entry : compiledClasses.entrySet()) {
                byteMap.put(entry.getKey(), entry.getValue().getCompiledBytes());  // Store compiled bytecode
            }
            return byteMap;  // Return the map of bytecode
        }
    }
}
