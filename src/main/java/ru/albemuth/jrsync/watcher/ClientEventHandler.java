package ru.albemuth.jrsync.watcher;

import ru.albemuth.jrsync.transport.Client;
import ru.albemuth.jrsync.transport.Command;

import java.io.IOException;

/**
 * @author vovan
 */
public class ClientEventHandler implements EventHandler {

    private Client client;

    public ClientEventHandler(Client client) {
        this.client = client;
    }

    @Override
    public void addClass(String className, byte[] classContent) {
        System.out.println("Class " + className + " created");
        send(new Event.Add(className, classContent));
    }

    @Override
    public void modifyClass(String className, byte[] classContent) {
        System.out.println("Class " + className + " modified");
        send(new Event.Modify(className, classContent));
    }

    @Override
    public void deleteClass(String className) {
        System.out.println("Class " + className + " deleted");
        send(new Event.Delete(className));
    }

    private void send(Event event) {
        try {
            client.send(new Command.JRSync(event));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
