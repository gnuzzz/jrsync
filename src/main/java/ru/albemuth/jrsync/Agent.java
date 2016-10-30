package ru.albemuth.jrsync;

import ru.albemuth.jrsync.transport.Client;
import ru.albemuth.jrsync.transport.Server;
import ru.albemuth.jrsync.watcher.ClassFileWatcher;
import ru.albemuth.jrsync.watcher.ClasspathWatcher;
import ru.albemuth.jrsync.watcher.ClientEventHandler;
import ru.albemuth.jrsync.watcher.LocalEventHandler;
import ru.albemuth.jrsync.watcher.ServerEventHandler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class Agent {

    public static void main(String[] args) throws Exception {
        Client client = new Client(args[0], Integer.parseInt(args[1]), Long.parseLong(args[2]));
        ClientEventHandler eventHandler = new ClientEventHandler(client);
        ClassFileWatcher classFileWatcher = new ClassFileWatcher(eventHandler, 1000, 1000);
        classFileWatcher.start();
        ClasspathWatcher watcher = new ClasspathWatcher(
                classFileWatcher,
                Arrays.asList(args[3].split(";")),
                5000);
        Thread thread = watcher.start();
        thread.join();
    }

    public static void premain(String args, Instrumentation instrumentation) {
        Pattern argsPattern = Pattern.compile("classpath=([^;]+);port=(\\d+)");
        Matcher m = argsPattern.matcher(args);
        if (!m.find()) {
            System.err.println("Invalid args: " + args);
            return;
        }
        ServerEventHandler eventHandler = new ServerEventHandler(m.group(1));
        try {
            Server server = new Server(eventHandler, Integer.parseInt(m.group(2)));
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

//System.out.println("agent args: " + args);
//        LocalEventHandler updateHandler = new LocalEventHandler(instrumentation);
//        ClassFileWatcher classFileWatcher = new ClassFileWatcher(updateHandler, 1000, 1000);
//        classFileWatcher.start();
//        ClasspathWatcher classpathWatcher = new ClasspathWatcher(
//                classFileWatcher,
//                Arrays.asList("D:\\Vovan\\lang\\albemuth\\jrsync\\target\\test-classes"),
//                5000);
//        classpathWatcher.start();
    }

}
