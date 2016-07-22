package se.vidstige.jadb;

import se.vidstige.jadb.managers.Bash;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JadbDevice {
    private final String serial;
    private final ITransportFactory transportFactory;

    JadbDevice(String serial, String type, ITransportFactory tFactory) {
        this.serial = serial;
        this.transportFactory = tFactory;
    }

    static JadbDevice createAny(JadbConnection connection) {
        return new JadbDevice(connection);
    }

    private JadbDevice(ITransportFactory tFactory) {
        serial = null;
        this.transportFactory = tFactory;
    }

    private Transport getTransport() throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        if (serial == null) {
            transport.send("host:transport-any");
            transport.verifyResponse();
        } else {
            transport.send("host:transport:" + serial);
            transport.verifyResponse();
        }
        return transport;
    }

    public String getSerial() {
        return serial;
    }

    public InputStream executeShell(String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = new StringBuilder(command);
        for (String arg : args) {
            shellLine.append(" ");
            shellLine.append(Bash.quote(arg));
        }
        send(transport, "shell:" + shellLine.toString());
        return new AdbFilterInputStream(new BufferedInputStream(transport.getInputStream()));
    }

    /**
     *
     * @deprecated Use InputStream executeShell(String command, String... args) method instead. Together with
     * Stream.copy(in, out), it is possible to achieve the same effect.
     */
    @Deprecated
    public void executeShell(OutputStream output, String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = new StringBuilder(command);
        for (String arg : args) {
            shellLine.append(" ");
            shellLine.append(Bash.quote(arg));
        }
        send(transport, "shell:" + shellLine.toString());
        if (output != null) {
            transport.readResponseTo(new AdbFilterOutputStream(output));
        }
    }

    public List<RemoteFile> list(String remotePath) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("LIST", remotePath);

        List<RemoteFile> result = new ArrayList<RemoteFile>();
        for (RemoteFileRecord dent = sync.readDirectoryEntry(); dent != RemoteFileRecord.DONE; dent = sync.readDirectoryEntry()) {
            result.add(dent);
        }
        return result;
    }

    private int getMode(File file) {
        //noinspection OctalInteger
        return 0664;
    }

    public void push(InputStream source, long lastModified, int mode, RemoteFile remote) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("SEND", remote.getPath() + "," + Integer.toString(mode));

        sync.sendStream(source);

        sync.sendStatus("DONE", (int) lastModified);
        sync.verifyStatus();
    }

    public void push(File local, RemoteFile remote) throws IOException, JadbException {
        FileInputStream fileStream = new FileInputStream(local);
        push(fileStream, local.lastModified(), getMode(local), remote);
        fileStream.close();
    }

    public void pull(RemoteFile remote, OutputStream destination) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("RECV", remote.getPath());

        sync.readChunksTo(destination);
    }

    public void pull(RemoteFile remote, File local) throws IOException, JadbException {
        FileOutputStream fileStream = new FileOutputStream(local);
        pull(remote, fileStream);
        fileStream.close();
    }

    private void send(Transport transport, String command) throws IOException, JadbException {
        transport.send(command);
        transport.verifyResponse();
    }

    @Override
    public String toString() {
        return "Android Device with serial " + serial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JadbDevice other = (JadbDevice) obj;
        if (serial == null) {
            if (other.serial != null)
                return false;
        } else if (!serial.equals(other.serial))
            return false;
        return true;
    }
}
