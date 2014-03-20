package se.vidstige.jadb;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JadbDevice {
	private String serial;
	private Transport transport;
	private boolean selected = false;

	JadbDevice(String serial, String type, Transport transport) {
		this.serial = serial;
		this.transport = transport;
	}

    static JadbDevice createAny(Transport transport) { return new JadbDevice(transport); }

    private JadbDevice(Transport transport)
    {
        serial = null;
        this.transport = transport;
    }

    private void ensureTransportIsSelected() throws IOException, JadbException {
        if (!selected)
        {
            selectTransport();
            selected = true;
        }
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

	public void executeShell(String command, String ... args) throws IOException, JadbException {
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

    public List<RemoteFileRecord> list(String remotePath) throws IOException, JadbException {
        ensureTransportIsSelected();
        SyncTransport sync  = transport.startSync();
        sync.send("LIST", remotePath);

        List<RemoteFileRecord> result = new ArrayList<RemoteFileRecord>();
        for (RemoteFileRecord dent = sync.readDirectoryEntry(); dent != RemoteFileRecord.DONE; dent = sync.readDirectoryEntry())
        {
            result.add(dent);
        }
        return result;
    }

    private int getMode(File file)
    {
        return 0664;
    }

    public void push(String localPath, String remotePath) throws IOException, JadbException {
		ensureTransportIsSelected();
        SyncTransport sync  = transport.startSync();
        File local = new File(localPath);
        sync.send("SEND", remotePath + "," + Integer.toString(getMode(local)));

        FileInputStream fileStream = new FileInputStream(local);
        sync.sendStream(fileStream);
        fileStream.close();

        sync.sendStatus("DONE", (int) local.lastModified());
        sync.verifyStatus();
	}

    public void pull(String remotePath, String localPath) throws IOException, JadbException {
        ensureTransportIsSelected();
        SyncTransport sync = transport.startSync();
        sync.send("RECV", remotePath);

        FileOutputStream fileStream = new FileOutputStream(new File(localPath));
        sync.readChunksTo(fileStream);
        fileStream.close();
    }

	private void send(String command) throws IOException, JadbException {
		transport.send(command);
        transport.verifyResponse();
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
