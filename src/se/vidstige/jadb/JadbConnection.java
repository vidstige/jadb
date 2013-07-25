package se.vidstige.jadb;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class JadbConnection {
	
	private Socket _socket;
	private static final int DEFAULTPORT = 5037;
	
	public JadbConnection() throws UnknownHostException, IOException
	{
		_socket = new Socket("localhost", DEFAULTPORT);
	}

	public void getHostVersion() throws IOException {
		send("host:version");
		
		DataInput reader = new DataInputStream(_socket.getInputStream());
		byte[] response = new byte[4];		
		reader.readFully(response);
		System.out.println(new String(response, Charset.forName("utf-8")));		
	}
	
	public void close() throws IOException
	{
		_socket.close();
	}

	private String getCommandLength(String command) {
		return String.format("%04x", Integer.valueOf(command.length()));
	}
	
	private void send(String command) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(_socket.getOutputStream());
		writer.write(getCommandLength(command));
		writer.write(command);
		writer.flush();
	}
}
