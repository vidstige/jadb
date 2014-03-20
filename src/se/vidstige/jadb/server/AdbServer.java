package se.vidstige.jadb.server;

import java.net.Socket;

/**
 * Created by vidstige on 2014-03-20
 */
public class AdbServer extends SocketServer {

    public static final int DEFAULT_PORT = 15037;

    public AdbServer()
    {
        this(DEFAULT_PORT);
    }

    public AdbServer(int port) {
        super(port);
    }

    @Override
    protected Runnable createResponder(Socket socket) {
        return new AdbProtocolHandler(socket);
    }

    public static void main(String[] args)
    {
        SocketServer server = new AdbServer();
        server.run();
    }
}
