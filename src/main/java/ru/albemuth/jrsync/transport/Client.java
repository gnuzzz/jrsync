package ru.albemuth.jrsync.transport;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author vovan
 */
public class Client {

    private String host;
    private int port;
    private long idleTimeout;
    private Socket socket;
    private ObjectOutputStream out;
    private Runnable watchdog;
    private long expires;

    public Client(String host, int port, long idleTimeout) {
        this.host = host;
        this.port = port;
        this.idleTimeout = idleTimeout;
    }

    public synchronized void send(Command command) throws IOException {
        if (socket == null) {
            openConnection();
        }
        out.writeObject(command);
        expires = System.currentTimeMillis() + idleTimeout;
    }

    private void openConnection() throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        watchdog = () -> {
            for (; true; ) {
                synchronized (Client.this) {
                    try {
                        Client.this.wait(idleTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (socket == null) break;
                    if (isExpired()) {
                        try {
                            out.writeObject(new Command.Close());
                            closeConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        };
        new Thread(watchdog).start();
    }

    private void closeConnection() throws IOException {
        watchdog = null;
        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } finally {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
    }

    private boolean isExpired() {
        return expires < System.currentTimeMillis();
    }

}
