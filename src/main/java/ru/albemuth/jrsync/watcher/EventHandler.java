package ru.albemuth.jrsync.watcher;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public interface EventHandler {

    void addClass(String existingClassName, String className, byte[] classContent);
    void modifyClass(String className, byte[] classContent);
    void deleteClass(String className);

    static EventHandler createEventHandler(Map<String, String> args, Instrumentation instrumentation) {
        EventHandler handler = null;
        try {
            Class eventHandlerClass = Class.forName(args.get("handler"));
            Method factoryMethod = eventHandlerClass.getDeclaredMethod("createEventHandler", Map.class, Instrumentation.class);
            handler = (EventHandler) factoryMethod.invoke(null, args, instrumentation);
        } catch (ClassNotFoundException e) {
            System.err.println("Can't create event handler of class " + args.get("handler") + ": " + e.getMessage());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.err.println("Can't create event handler of class " + args.get("handler") +
                    ": factory method createEventHandler(Map<String, String> args) not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("Can't create event handler instance of class " + args.get("handler") + ": " + e.getMessage());
            e.printStackTrace();
        }
        return handler;
    }

}
