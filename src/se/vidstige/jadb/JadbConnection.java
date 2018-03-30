package se.vidstige.jadb;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class JadbConnection {

    private final ITransportFactory transportFactory;

    public JadbConnection() {
        this(new SocketTransportFactory());
    }

    public JadbConnection(ITransportFactory transportFactory) {
        this.transportFactory = transportFactory;
    }

    public JadbConnection(String host, int port) {
        transportFactory = new SocketTransportFactory(host,port);
    }

    final ITransportFactory getTransportFactory() {
        return transportFactory;
    }

    public String getHostVersion() throws IOException, JadbException {
        Transport main = transportFactory.createTransport();
        main.send("host:version");
        main.verifyResponse();
        String version = main.readString();
        main.close();
        return version;
    }

    public InetSocketAddress connectToTcpDevice(InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        Transport transport = transportFactory.createTransport();
        try {
            return new HostConnectToRemoteTcpDevice(transport).connect(inetSocketAddress);
        } finally {
            transport.close();
        }
    }

    public InetSocketAddress disconnectFromTcpDevice(InetSocketAddress tcpAddressEntity)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        Transport transport = transportFactory.createTransport();
        try {
            return new HostDisconnectFromRemoteTcpDevice(transport).disconnect(tcpAddressEntity);
        } finally {
            transport.close();
        }
    }

    public List<JadbDevice> getDevices() throws IOException, JadbException {
        Transport devices = transportFactory.createTransport();
        devices.send("host:devices");
        devices.verifyResponse();
        String body = devices.readString();
        devices.close();
        return parseDevices(body);
    }

    public DeviceWatcher createDeviceWatcher(DeviceDetectionListener listener) throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        transport.send("host:track-devices");
        transport.verifyResponse();
        return new DeviceWatcher(transport, listener, this);
    }

    public List<JadbDevice> parseDevices(String body) {
        String[] lines = body.split("\n");
        ArrayList<JadbDevice> devices = new ArrayList<JadbDevice>(lines.length);
        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length > 1) {
                devices.add(new JadbDevice(parts[0], parts[1], transportFactory));
            }
        }
        return devices;
    }

    public JadbDevice getAnyDevice() {
        return JadbDevice.createAny(this);
    }
}
