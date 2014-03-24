package se.vidstige.jadb.server;

import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.charset.Charset;

public class AdbProtocolHandler implements Runnable {
	private final Socket socket;
    private final AdbResponder responder;

    public AdbProtocolHandler(Socket socket, AdbResponder responder) {
		this.socket = socket;
        this.responder = responder;
    }

    private AdbDeviceResponder findDevice(String serial) throws ProtocolException {
        for (AdbDeviceResponder d : responder.getDevices())
        {
            if (d.getSerial().equals(serial)) return d;
        }
        throw new ProtocolException("'" + serial + "' not connected");
    }

	@Override
	public void run()
	{
        try{
            runServer();
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }

    private void runServer() throws IOException {
        DataInput input = new DataInputStream(socket.getInputStream());
        OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());

        while (true)
        {
            byte[] buffer = new byte[4];
            input.readFully(buffer);
            String encodedLength = new String(buffer, Charset.forName("utf-8"));
            int length = Integer.parseInt(encodedLength, 16);

            buffer = new byte[length];
            input.readFully(buffer);
            String command = new String(buffer, Charset.forName("utf-8"));

            responder.onCommand(command);

            try
            {
                if ("host:version".equals(command)) {
                    output.write("OKAY");
                    send(output, String.format("%04x", responder.getVersion()));
                }
                else if ("host:transport-any".equals(command))
                {
                    // TODO: Check so that exactly one device is selected.
                    output.write("OKAY");
                }
                else if ("host:devices".equals(command)) {
                    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                    DataOutputStream writer = new DataOutputStream(tmp);
                    for (AdbDeviceResponder d : responder.getDevices())
                    {
                        writer.writeBytes(d.getSerial() + "\t" + d.getType() + "\n");
                    }
                    output.write("OKAY");
                    send(output, new String(tmp.toByteArray(), Charset.forName("utf-8")));
                }
                else if (command.startsWith("host:transport:"))
                {
                    String serial = command.substring("host:transport:".length());
                    findDevice(serial);
                }
                else
                {
                    throw new ProtocolException("Unknown command: " + command);
                }
            } catch (ProtocolException e) {
                output.write("FAIL");
                send(output, e.getMessage());
            }
            output.flush();
        }
    }
	
	private String getCommandLength(String command) {
		return String.format("%04x", command.length());
	}
	
	public void send(OutputStreamWriter writer, String response) throws IOException {
		writer.write(getCommandLength(response));
		writer.write(response);
	}
}
