package ru.albemuth.jrsync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.albemuth.jrsync.watcher.EventHandler;

/**
 * @author vovan
 */
public class SimpleEventHandler implements EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventHandler.class);

    @Override
    public void addClass(String className, byte[] classContent) {
        LOGGER.info("Class {} created", className);
    }

    @Override
    public void modifyClass(String className, byte[] classContent) {
        LOGGER.info("Class {} modified", className);
    }

    @Override
    public void deleteClass(String className) {
        LOGGER.info("Class {} deleted", className);
    }
}
