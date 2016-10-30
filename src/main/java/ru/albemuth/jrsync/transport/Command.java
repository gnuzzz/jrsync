package ru.albemuth.jrsync.transport;

import ru.albemuth.jrsync.watcher.Event;

import java.io.Serializable;

/**
 * @author vovan
 */
public abstract class Command implements Serializable {

    public static class Close extends Command {

    }

    public static abstract class Content<T> extends Command {

        private final T content;

        public Content(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }
    }

    public static class JRSync extends Content<Event> {

        public JRSync(Event content) {
            super(content);
        }
    }

}
