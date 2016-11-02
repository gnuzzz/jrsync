package ru.albemuth.jrsync;

import ru.albemuth.jrsync.transport.Client;
import ru.albemuth.jrsync.watcher.ClassFileQueue;
import ru.albemuth.jrsync.watcher.ClasspathWatcher;
import ru.albemuth.jrsync.watcher.ClientEventHandler;

import java.util.Arrays;
import java.util.List;

import static ru.albemuth.jrsync.watcher.ClassUtils.existingClasses;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class AgentSampleTest {

    public static void main(String[] args) throws Exception {
        Client client = new Client("localhost", 1111, 1000);
        ClientEventHandler eventHandler = new ClientEventHandler(client);
        List<String> roots = Arrays.asList("target/test-classes");
        ClassFileQueue classFileQueue = new ClassFileQueue(eventHandler, 1000, 5000, existingClasses(roots));
        classFileQueue.start();
        ClasspathWatcher watcher = new ClasspathWatcher(
                classFileQueue,
                roots,
                5000);
        Thread thread = watcher.start();
        thread.join();
    }

}
