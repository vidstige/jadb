package se.vidstige.jadb.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// >set ANDROID_ADB_SERVER_PORT=15037
public abstract class SocketServer implements Runnable {

    private final int port;
    private ServerSocket socket;
    private Thread thread;

    private boolean isStarted = false;
    private final Object lockObject = new Object();

    protected SocketServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        thread = new Thread(this, "Fake Adb Server");
        thread.setDaemon(true);
        thread.start();
        serverReady();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(port);
            socket.setReuseAddress(true);

            waitForServer();

            while (true) {
                Socket c = socket.accept();
                Thread clientThread = new Thread(createResponder(c), "AdbClientWorker");
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
        }
    }

    private void serverReady() throws InterruptedException {
        synchronized (lockObject) {
            if (!isStarted) {
                lockObject.wait();
            }
        }
    }

    private void waitForServer() {
        synchronized (lockObject) {
            lockObject.notify();
            isStarted = true;
        }
    }

    protected abstract Runnable createResponder(Socket socket);

    public void stop() throws IOException, InterruptedException {
        socket.close();
        thread.join();
    }
}
