package se.vidstige.jadb;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class JadbConnection {
	
	private Socket _socket;
	private static final int DEFAULTPORT = 5037;
	
	public JadbConnection() throws UnknownHostException, IOException
	{
		_socket = new Socket("localhost", DEFAULTPORT);
				
	}

	public void getHostVersion() throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(_socket.getOutputStream());
		DataInput reader = new DataInputStream(_socket.getInputStream());
		writer.write("000Chost:version");
		writer.flush();
		byte[] response = new byte[4];
		reader.readFully(response);
		System.out.println(new String(response, Charset.forName("utf-8")));		
	}
	
	public void close() throws IOException
	{
		_socket.close();
	}
}
