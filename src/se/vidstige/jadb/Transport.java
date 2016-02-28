package se.vidstige.jadb;


import org.apache.commons.io.IOUtils;

import java.io.*;
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

	public String readResponse() throws IOException {
		return new String(IOUtils.toByteArray(inputStream), Charset.forName("utf-8"));
	}

	public byte[] readResponseAsArray() throws IOException {
		return repairTransportedArray(IOUtils.toByteArray(inputStream));
	}

	public void verifyResponse() throws IOException, JadbException  {
		String response = readString(4);
		if (!"OKAY".equals(response))
        {
            String error = readString();
            throw new JadbException("command failed: " + error);
        }
	}

	public String readString(int length) throws IOException {
		DataInput reader = new DataInputStream(inputStream);
		byte[] responseBuffer = new byte[length];		
		reader.readFully(responseBuffer);
        return new String(responseBuffer, Charset.forName("utf-8"));
	}

	public String getCommandLength(String command) {
		return String.format("%04x", command.length());
	}
	
	public void send(String command) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		writer.write(getCommandLength(command));
		writer.write(command);
		writer.flush();
	}

    public SyncTransport startSync() throws IOException, JadbException {
        send("sync:");
        verifyResponse();
        return new SyncTransport(outputStream, inputStream);
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }

	private byte[] repairTransportedArray(byte[] encoded) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i=0; i<encoded.length; i++) {
			if (encoded.length > i+1 && encoded[i] == 0x0d && encoded[i+1] == 0x0a) {
				//skip 0x0d
			} else {
				baos.write(encoded[i]);
			}
		}
		try {
			baos.close();
		} catch (IOException ioe) {

		}
		return baos.toByteArray();
	}
}
