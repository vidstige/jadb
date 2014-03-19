package se.vidstige.jadb;

import java.io.IOException;

public class AndroidDevice {
	private String serial;
	private Transport transport;
	private boolean selected = false;

	AndroidDevice(String serial, String type, Transport transport) {
		this.serial = serial;
		this.transport = transport;
	}

    static AndroidDevice createAny(Transport transport) { return new AndroidDevice(transport); }

    private AndroidDevice(Transport transport)
    {
        serial = null;
        this.transport = transport;
    }

    private void selectTransport() throws IOException, JadbException {
        if (!selected)
        {
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
            selected = true;
        }
    }

	public String getSerial()
	{
		return serial;
	}

	public String getState() throws IOException, JadbException {
        selectTransport();
		transport.send(getPrefix() +  "get-state");
		transport.verifyResponse();
		return transport.readString();
	}

	public void executeShell(String command, String ... args) throws IOException, JadbException {
        selectTransport();

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

    public void list(String remotePath) throws IOException, JadbException {
        selectTransport();
        SyncTransport sync  = transport.startSync();
        sync.send("LIST", remotePath);

        for (RemoteFile dent = sync.readDirectoryEntry(); dent != RemoteFile.DONE; dent = sync.readDirectoryEntry())
        {
            System.out.println(dent.getName());
        }
    }

    public void push(String localPath, String remotePath) throws IOException, JadbException {
		selectTransport();
	}

	private void send(String command) throws IOException, JadbException {
		transport.send(command);
        transport.verifyResponse();
	}
	
	private String getPrefix() {
		//return "host-serial:" + serial + ":";
		return "host-local:";
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
		AndroidDevice other = (AndroidDevice) obj;
		if (serial == null) {
			if (other.serial != null)
				return false;
		} else if (!serial.equals(other.serial))
			return false;
		return true;
	}
}
