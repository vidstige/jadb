package se.vidstige.jadb;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class JadbConnection {
	
	private final Socket _socket;
	private static final int DEFAULTPORT = 5037;
	
	private final Transport transport;
	
	public JadbConnection() throws UnknownHostException, IOException
	{
		_socket = new Socket("localhost", DEFAULTPORT);
		transport = new Transport(_socket.getOutputStream(), _socket.getInputStream());
	}

	public void getHostVersion() throws IOException, JadbException {
		transport.send("host:version");
		transport.verifyResponse();
	}

	public List<AndroidDevice> getDevices() throws IOException, JadbException
	{
		transport.send("host:devices");
		transport.verifyResponse();
		String body = transport.readString();
		return parseDevices(body);
	}	

	public void close() throws IOException
	{
		_socket.close();
	}	

	private List<AndroidDevice> parseDevices(String body) {
		String[] lines = body.split("\n");
		ArrayList<AndroidDevice> devices = new ArrayList<AndroidDevice>(lines.length);
		for (String line : lines)
		{
			String[] parts = line.split("\t");
			devices.add(new AndroidDevice(parts[0], parts[1]));
		}
		return devices;
	}
}
