package se.vidstige.jadb;

import java.util.List;
import java.io.IOException;

public class DeviceDetectionHandler {
    private Transport transport;
    public DeviceDetectionHandler(Transport transport) {
        this.transport = transport;
    };

    public void stop() throws IOException {
        transport.close();
    }
}
