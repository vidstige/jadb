package se.vidstige.jadb;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JadbConnection implements ITransportFactory {

    private final String host;
    private final int port;

    private static final int DEFAULTPORT = 5037;

    public JadbConnection() throws IOException {
        this("localhost", DEFAULTPORT);
    }

    public JadbConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public Transport createTransport() throws IOException {
        return new Transport(new Socket(host, port));
    }

    public void connect(String ip, int port) throws IOException, JadbException {
        Transport main = this.createTransport();
        main.send("host:connect:" + ip + ":" + port);
        main.verifyResponse();
        main.close();
    }

    public void disconnect(String ip, int port) throws IOException, JadbException {
        Transport main = this.createTransport();
        main.send("host:disconnect:" + ip + ":" + port);
        main.verifyResponse();
        main.close();
    }

    public void getHostVersion() throws IOException, JadbException {
        Transport main = createTransport();
        main.send("host:version");
        main.verifyResponse();
        main.close();
    }

    public List<JadbDevice> getDevices() throws IOException, JadbException {
        Transport devices = createTransport();

        devices.send("host:devices");
        devices.verifyResponse();
        String body = devices.readString();
        return parseDevices(body);
    }

    private List<JadbDevice> parseDevices(String body) {
        String[] lines = body.split("\n");
        ArrayList<JadbDevice> devices = new ArrayList<JadbDevice>(lines.length);
        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length > 1) {
                devices.add(new JadbDevice(parts[0], parts[1], this));
            }
        }
        return devices;
    }

    public JadbDevice getAnyDevice() {
        return JadbDevice.createAny(this);
    }
}
