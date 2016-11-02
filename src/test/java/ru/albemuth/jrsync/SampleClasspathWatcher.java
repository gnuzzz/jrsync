package ru.albemuth.jrsync;

import ru.albemuth.jrsync.watcher.ClassFileQueue;
import ru.albemuth.jrsync.watcher.ClasspathWatcher;

import java.util.Arrays;
import java.util.List;

import static ru.albemuth.jrsync.watcher.ClassUtils.existingClasses;

/**
 * @author vovan
 */
public class SampleClasspathWatcher {

    public static void main(String[] args) throws Exception {
        List<String> roots = Arrays.asList("target\\test-classes");
        ClassFileQueue classFileQueue = new ClassFileQueue(new SimpleEventHandler(), 1000, 1000, existingClasses(roots));
        classFileQueue.start();
        ClasspathWatcher watcher = new ClasspathWatcher(classFileQueue, roots, 5000);
        Thread thread = watcher.start();
        thread.join();
    }
}
