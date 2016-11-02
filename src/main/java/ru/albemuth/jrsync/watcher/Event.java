package ru.albemuth.jrsync.watcher;

import java.io.Serializable;

/**
 * @author vovan
 */
public abstract class Event implements Serializable {

    public abstract void apply(EventHandler handler);

    public static class Add extends Event {

        private final String existingClassName;
        private final String className;
        private final byte[] classContent;

        public Add(String existingClassName, String className, byte[] classContent) {
            this.existingClassName = existingClassName;
            this.className = className;
            this.classContent = classContent;
        }

        @Override
        public void apply(EventHandler handler) {
            handler.addClass(existingClassName, className, classContent);
        }
    }

    public static class Modify extends Event {

        private final String className;
        private final byte[] classContent;

        public Modify(String className, byte[] classContent) {
            this.className = className;
            this.classContent = classContent;
        }

        @Override
        public void apply(EventHandler handler) {
            handler.modifyClass(className, classContent);
        }
    }

    public static class Delete extends Event {

        private final String className;

        public Delete(String className) {
            this.className = className;
        }

        @Override
        public void apply(EventHandler handler) {
            handler.deleteClass(className);
        }
    }

}
