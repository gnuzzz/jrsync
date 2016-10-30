package ru.albemuth.jrsync.watcher;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * @author vovan
 */
public class LocalEventHandler implements EventHandler {

    private Instrumentation instrumentation;

    public LocalEventHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void addClass(String className, byte[] classContent) {
        System.out.println("Class " + className + " created");
    }

    @Override
    public void modifyClass(String className, byte[] classContent) {
        System.out.println("Class " + className + " modified");
        for (Class clazz: instrumentation.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                try {
                    ClassUtils.defineClass(instrumentation, clazz, classContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deleteClass(String className) {
        System.out.println("Class " + className + " deleted");
    }
}
