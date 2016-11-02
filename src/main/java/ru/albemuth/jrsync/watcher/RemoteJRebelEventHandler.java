package ru.albemuth.jrsync.watcher;

import org.zeroturnaround.javarebel.Integration;
import org.zeroturnaround.javarebel.IntegrationFactory;
import ru.albemuth.jrsync.transport.Server;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class RemoteJRebelEventHandler implements EventHandler {

    private Instrumentation instrumentation;

    public RemoteJRebelEventHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void addClass(String existingClassName, String className, byte[] classContent) {
        Integration integration = IntegrationFactory.getInstance();
        for (ClassLoader cl: getClassLoaders()) {
            try {
                Class clazz = Class.forName(className, true, cl);
                //если класс найден - то это не создание, а модификация класса
                integration.redefineClasses(new ClassDefinition(clazz, classContent));
            } catch (ClassNotFoundException e) {
                //если класс не найден - то это создание класса
                try {
                    Class existingClazz = Class.forName(existingClassName, true, cl);
                    integration.defineReloadableClass(cl, className, classContent, existingClazz.getProtectionDomain());
                } catch (ClassNotFoundException ex) {
                    //do nothing
                }
            }
        }
    }

    @Override
    public void modifyClass(String className, byte[] classContent) {
        Integration integration = IntegrationFactory.getInstance();
        for (ClassLoader cl: getClassLoaders()) {
            try {
                Class clazz = Class.forName(className, true, cl);
                integration.redefineClasses(new ClassDefinition(clazz, classContent));
            } catch (ClassNotFoundException e) {
                //do nothing
            }
        }
    }

    @Override
    public void deleteClass(String className) {
        //do nothing
    }

    private Set<ClassLoader> getClassLoaders() {
        return Stream.of(instrumentation.getAllLoadedClasses()).map(Class::getClassLoader).collect(Collectors.toSet());
    }

    public static RemoteJRebelEventHandler createEventHandler(Map<String, String> args, Instrumentation instrumentation) {
        RemoteJRebelEventHandler eventHandler = new RemoteJRebelEventHandler(instrumentation);
        try {
            Server server = new Server(eventHandler, Integer.parseInt(args.get("port")));
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eventHandler;
    }

}
