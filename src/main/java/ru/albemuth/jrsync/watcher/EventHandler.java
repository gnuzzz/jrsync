package ru.albemuth.jrsync.watcher;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public interface EventHandler {

    void addClass(String className, byte[] classContent);
    void modifyClass(String className, byte[] classContent);
    void deleteClass(String className);

}
