package se.vidstige.jadb;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FakeJadbDevice  extends JadbDevice {
    private final List<InputStream> executeShellResponse;

    public FakeJadbDevice(String serial, String type, ITransportFactory tFactory, List<InputStream> executeShellResponse) {
        super(serial, type, tFactory);
        this.executeShellResponse = executeShellResponse;
    }

    @Override
    public InputStream executeShell(String command, String... args) throws IOException, JadbException {
        return executeShellResponse.get(0);
    }
}
