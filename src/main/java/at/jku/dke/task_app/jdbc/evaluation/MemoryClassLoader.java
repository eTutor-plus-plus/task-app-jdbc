package at.jku.dke.task_app.jdbc.evaluation;

import java.util.Map;

/**
 * Custom class loader that loads classes from a map of byte arrays.
 * This class is used to load classes that have been compiled dynamically and stored in memory.
 * It extends the default 'ClassLoader' and overrides the 'findClass' method to locate classes in the provided map.
 */
public class MemoryClassLoader extends ClassLoader {

    // Map that holds the class name (key) and the corresponding class bytecode
    private final Map<String, byte[]> classes;

    /**
     * Constructor that accepts a map of compiled class bytecode.
     *
     * @param classes A map where the key is the class name (e.g., "com.michael.TestClass") and the value is the compiled bytecode.
     */
    public MemoryClassLoader(Map<String, byte[]> classes) {
        // Calling the parent class (ClassLoader) constructor and passing the default class loader
        super(CodeRunner.class.getClassLoader());
        this.classes = classes;
    }

    /**
     * This method is called to find and load a class.
     * If the class is found in the 'classes' map, it is defined from memory. If not, it delegates the search to the parent class loader.
     *
     * @param name The name of the class to be loaded (e.g., "com.michael.TestClass").
     * @return The Class object for the requested class.
     * @throws ClassNotFoundException If the class could not be found.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Try to get the bytecode for the class from the provided map
        byte[] bytes = classes.get(name);

        // If the class is not found in the map, delegate the class loading to the parent class loader
        if (bytes == null) return super.findClass(name);

        // Define the class from the bytecode and return the Class object
        return defineClass(name, bytes, 0, bytes.length);
    }
}
