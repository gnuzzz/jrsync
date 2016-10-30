package ru.albemuth.jrsync.watcher;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import static ru.albemuth.jrsync.watcher.ClassUtils.readClassContent;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class JRSync {

    private static JRSync JRSync = null;

    private Instrumentation instrumentation;

    private JRSync(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void modifyClass(String className, byte[] classContent) {
        for (Class clazz: instrumentation.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                try {
                    defineClass(instrumentation, clazz, classContent);
                    System.out.println("Class " + className + " changed");
                } catch (IOException e) {
                    System.err.println("Can't modify class " + className);
                    e.printStackTrace();
                }
            }
        }
    }

    public void restoreClass(String className) {
        for (Class clazz: instrumentation.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                restoreClass(clazz);
            }
        }
    }

    private void restoreClass(Class clazz) {
        try {
            defineClass(instrumentation, clazz, ClassUtils.readClassContent(clazz));
            System.out.println("Class " + clazz.getName() + " restored");
        } catch (IOException e) {
            System.err.println("Can't restore class " + clazz.getName());
            e.printStackTrace();
        }
    }

    private void defineClass(Instrumentation instrumentation, Class clazz, byte[] classContent) throws IOException {
        try {
            ClassDefinition classDef = new ClassDefinition(clazz, classContent);
            instrumentation.redefineClasses(classDef);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            System.err.println("Can't modify class " + clazz.getName());
            e.printStackTrace();
        }
    }

    public static void init(Instrumentation instrumentation) {
        JRSync = new JRSync(instrumentation);
    }

    public static JRSync getJRSync() {
        return JRSync;
    }

}
