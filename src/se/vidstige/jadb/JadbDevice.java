package se.vidstige.jadb;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JadbDevice {
	private final String serial;
	private Transport transport;
    private final JadbConnection connection;

	JadbDevice(String serial, String type, JadbConnection connection) {
		this.serial = serial;
        this.connection = connection;
		this.transport = connection.getMain();
	}

    static JadbDevice createAny(JadbConnection connection) { return new JadbDevice(connection); }

    private JadbDevice(JadbConnection connection)
    {
        serial = null;
        this.connection = connection;
        this.transport = connection.getMain();
    }

    private void ensureTransportIsSelected() throws IOException, JadbException {
            selectTransport();
    }

    private void selectTransport() throws IOException, JadbException {
        if (serial == null)
        {
            transport.send("host:transport-any");
            transport.verifyResponse();
        }
        else
        {
            transport.send("host:transport:" + serial);
            transport.verifyResponse();

        }
    }

    public String getSerial()
	{
		return serial;
	}

	public String getState() throws IOException, JadbException {
        ensureTransportIsSelected();
		transport.send("get-state");
		transport.verifyResponse();
		return transport.readString();
	}

	public String executeShell(String command, String ... args) throws IOException, JadbException {
        execShell(command, args);
        String ret = this.transport.readResponse();
        reOpenTransport();
        return ret;
	}

    public byte[] executeShellGetBytearr(String command, String ... args) throws IOException, JadbException {
        execShell(command, args);
        byte[] ret = this.transport.readResponseAsArray();
        reOpenTransport();
        return ret;
    }

    private void execShell(String command, String[] args) throws IOException, JadbException {
        ensureTransportIsSelected();

        StringBuilder shellLine = new StringBuilder(command);
        for (String arg : args)
        {
            shellLine.append(" ");
            // TODO: throw if arg contains double quote
            // TODO: quote arg if it contains space
            shellLine.append(arg);
        }
        send("shell:" + shellLine.toString());
    }

    public List<RemoteFile> list(String remotePath) throws IOException, JadbException {
        ensureTransportIsSelected();
        SyncTransport sync  = transport.startSync();
        sync.send("LIST", remotePath);

        List<RemoteFile> result = new ArrayList<RemoteFile>();
        for (RemoteFileRecord dent = sync.readDirectoryEntry(); dent != RemoteFileRecord.DONE; dent = sync.readDirectoryEntry())
        {
            result.add(dent);
        }
        reOpenTransport();
        return result;
    }

    private int getMode(File file)
    {
        return 0664;
    }

    public void push(InputStream source, long lastModified, int mode, RemoteFile remote) throws IOException, JadbException {
        ensureTransportIsSelected();
        SyncTransport sync  = transport.startSync();
        sync.send("SEND", remote.getPath() + "," + Integer.toString(mode));

        sync.sendStream(source);

        sync.sendStatus("DONE", (int)lastModified);
        sync.verifyStatus();
        reOpenTransport();
    }

    public void push(File local, RemoteFile remote) throws IOException, JadbException {
        FileInputStream fileStream = new FileInputStream(local);
        push(fileStream, local.lastModified(), getMode(local), remote);
        fileStream.close();
	}

    public void pull(RemoteFile remote, OutputStream destination) throws IOException, JadbException {
        ensureTransportIsSelected();
        SyncTransport sync = transport.startSync();
        sync.send("RECV", remote.getPath());

        sync.readChunksTo(destination);
        reOpenTransport();
    }

    public void pull(RemoteFile remote, File local) throws IOException, JadbException {
        FileOutputStream fileStream = new FileOutputStream(local);
        pull(remote, fileStream);
        fileStream.close();
    }

	private void send(String command) throws IOException, JadbException {
		transport.send(command);
        transport.verifyResponse();
	}

    private void reOpenTransport() throws IOException {
        transport.close();
        transport = connection.getFreshTransport();
    }
	
	@Override
	public String toString()
	{
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
