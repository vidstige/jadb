package se.vidstige.jadb.test.fakes;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class AdbResponder implements Runnable {
	private Socket socket;

	public AdbResponder(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run()
	{		
		try {
			System.out.println("Serving client");
			DataInput input = new DataInputStream(socket.getInputStream());
			OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
			byte[] buffer = new byte[4];
			input.readFully(buffer);
			String encodedLength = new String(buffer, Charset.forName("utf-8"));
			int length = Integer.parseInt(encodedLength, 16);
			
			buffer = new byte[length];
			input.readFully(buffer);
			String command = new String(buffer, Charset.forName("utf-8"));
			System.out.println("Command: " + command);
			
			if ("host.devices".equals(command)) {
				output.write("OKAY");
				send(output, "X\tdevice\nY\tdevice");	
			}
			output.write("FAIL");
			
		} catch (IOException e) {
		}		
	}
	
	private String getCommandLength(String command) {
		return String.format("%04x", Integer.valueOf(command.length()));
	}
	
	public void send(OutputStreamWriter writer, String response) throws IOException {
		writer.write(getCommandLength(response));
		writer.write(response);
		writer.flush();
	}
}
