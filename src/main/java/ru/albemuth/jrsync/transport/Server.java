package ru.albemuth.jrsync.transport;

import ru.albemuth.jrsync.watcher.EventHandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vovan
 */
public class Server implements Closeable, Runnable {

    private EventHandler eventHandler;
    private ServerSocket serverSocket;
    private boolean running;
    private Set<Handler> handlers;

    public Server(EventHandler eventHandler, int port) throws IOException {
        this.eventHandler = eventHandler;
        this.serverSocket = new ServerSocket(port);
        this.handlers = new HashSet<>();
        System.out.println("Listening port: " + port);
    }

    @Override
    public synchronized void close() throws IOException {
        serverSocket.close();
        running = false;
        for (Handler handler: handlers) {
            handler.close();
        }
    }

    @Override
    public void run() {
        running = true;
        for (; running; ) {
            try {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                new Thread(handler).run();
                synchronized (this) {
                    handlers.add(handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Handler implements Closeable, Runnable {

        private Socket socket;
        private boolean running;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public synchronized void close() throws IOException {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                for (; running; ) {
                    Command command = (Command) in.readObject();
                    if (command instanceof Command.Close) {
                        running = false;
                    } else if (command instanceof Command.JRSync) {
                        ((Command.JRSync) command).getContent().apply(eventHandler);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (Server.this) {
                handlers.remove(this);
            }
        }
    }
}
