package se.vidstige.jadb;

import java.util.List;
import java.io.IOException;

public class AsyncActionHandler {
    private Transport transport;
    public AsyncActionHandler(Transport transport) {
        this.transport = transport;
    };

    public void stop() throws IOException {
        transport.close();
    }
}
