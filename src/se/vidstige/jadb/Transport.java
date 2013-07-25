package se.vidstige.jadb;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

class Transport {

	private final OutputStream outputStream;
	private final InputStream inputStream;

	private Transport(OutputStream outputStream, InputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	public Transport(Socket socket) throws IOException {
		this(socket.getOutputStream(), socket.getInputStream());
	}

	public String readString() throws IOException {
		String encodedLength = readString(4);
		int length = Integer.parseInt(encodedLength, 16); 		
		return readString(length);
	}
	
	public void verifyResponse() throws IOException, JadbException  {
		String response = readString(4);
		if ("OKAY".equals(response) == false) throw new JadbException("command failed");
	}

	public String readString(int length) throws IOException {
		DataInput reader = new DataInputStream(inputStream);
		byte[] responseBuffer = new byte[length];		
		reader.readFully(responseBuffer);
		String response = new String(responseBuffer, Charset.forName("utf-8"));
		return response;
	}

	public String getCommandLength(String command) {
		return String.format("%04x", Integer.valueOf(command.length()));
	}
	
	public void send(String command) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		writer.write(getCommandLength(command));
		writer.write(command);
		writer.flush();
	}
}
