package se.vidstige.jadb;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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

	public void getDevices() throws IOException, JadbException
	{
		transport.send("host:devices");
		transport.verifyResponse();
		String body = transport.readString();
		System.out.println(body);		
	}

	public void close() throws IOException
	{
		_socket.close();
	}
}
