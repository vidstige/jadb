package se.vidstige.jadb;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JadbConnection {
	
	private final String host;
	private final int port;
	
	private static final int DEFAULTPORT = 5037;
	
	private final Transport main;
	
	public JadbConnection() throws IOException
	{
		this("localhost", DEFAULTPORT);
	}
	
	public JadbConnection(String host, int port) throws IOException
	{
		this.host = host;
		this.port = port;
				
		main = createTransport();
	}

	private Transport createTransport() throws IOException {
		return new Transport(new Socket(host, port));
	}

	public void getHostVersion() throws IOException, JadbException {
		main.send("host:version");
		main.verifyResponse();
	}

	public List<AndroidDevice> getDevices() throws IOException, JadbException
	{
		Transport devices = createTransport();
		
		devices.send("host:devices");
		devices.verifyResponse();
		String body = devices.readString();
		return parseDevices(body);
	}

	private List<AndroidDevice> parseDevices(String body) {
		String[] lines = body.split("\n");
		ArrayList<AndroidDevice> devices = new ArrayList<AndroidDevice>(lines.length);
		for (String line : lines)
		{
			String[] parts = line.split("\t");
			devices.add(new AndroidDevice(parts[0], parts[1], main));
		}
		return devices;
	}

    public AndroidDevice getAnyDevice() {
        return AndroidDevice.createAny(main);
    }
}
