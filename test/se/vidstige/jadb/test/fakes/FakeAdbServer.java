package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.server.AdbDeviceResponder;
import se.vidstige.jadb.server.AdbResponder;
import se.vidstige.jadb.server.AdbServer;

import java.io.IOException;

/**
 * Created by vidstige on 2014-03-20.
 */
public class FakeAdbServer implements AdbResponder, AdbDeviceResponder {
    private final AdbServer server;

    public FakeAdbServer(int port) {
        server = new AdbServer(port);
    }


    public void start() throws InterruptedException {
        server.start();
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
    }
}
