package ru.albemuth.jrsync;

import ru.albemuth.jrsync.transport.Client;
import ru.albemuth.jrsync.watcher.ClassFileQueue;
import ru.albemuth.jrsync.watcher.ClasspathWatcher;
import ru.albemuth.jrsync.watcher.ClientEventHandler;
import ru.albemuth.jrsync.watcher.EventHandler;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.albemuth.jrsync.watcher.ClassUtils.existingClasses;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class Agent {

    public static void main(String[] args) throws Exception {
        Client client = new Client(args[0], Integer.parseInt(args[1]), Long.parseLong(args[2]));
        ClientEventHandler eventHandler = new ClientEventHandler(client);
        List<String> roots = Arrays.asList(args[3].split(";"));
        ClassFileQueue classFileQueue = new ClassFileQueue(eventHandler, 1000, 5000, existingClasses(roots));
        classFileQueue.start();
        ClasspathWatcher watcher = new ClasspathWatcher(
                classFileQueue,
                roots,
                5000);
        Thread thread = watcher.start();
        thread.join();
    }

    public static void premain(String args, Instrumentation instrumentation) {
        EventHandler.createEventHandler(getArgsMap(args), instrumentation);
    }

    private static Map<String, String> getArgsMap(String args) {
        Map<String, String> argsMap = new HashMap<>();
        for (String arg: args.split(";")) {
            String[] keyValue = arg.split("=");
            if (keyValue.length == 2) argsMap.put(keyValue[0], keyValue[1]);
        }
        return argsMap;
    }

}
