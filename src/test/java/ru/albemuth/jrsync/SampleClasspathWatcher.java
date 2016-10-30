package ru.albemuth.jrsync;

import org.junit.Test;
import ru.albemuth.jrsync.watcher.ClassFileWatcher;
import ru.albemuth.jrsync.watcher.ClasspathWatcher;

import java.util.Arrays;

/**
 * @author vovan
 */
public class SampleClasspathWatcher {

    public static void main(String[] args) throws Exception {
        ClassFileWatcher classFileWatcher = new ClassFileWatcher(new SimpleEventHandler(), 1000, 1000);
        classFileWatcher.start();
        ClasspathWatcher watcher = new ClasspathWatcher(
                classFileWatcher,
                Arrays.asList("target\\test-classes"),
                5000);
        Thread thread = watcher.start();
        thread.join();
    }
}
