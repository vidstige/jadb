package se.vidstige.jadb.server;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.SyncTransport;

import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.charset.Charset;

class AdbProtocolHandler implements Runnable {
	private final Socket socket;
    private final AdbResponder responder;
    private AdbDeviceResponder selected;

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
            if (e.getMessage() != null) // thrown when exiting for some reason
                System.out.println("IO Error: " + e.getMessage());
        }
    }

    private void runServer() throws IOException {
        DataInput input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

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
                    output.writeBytes("OKAY");
                    send(output, String.format("%04x", responder.getVersion()));
                }
                else if ("host:transport-any".equals(command))
                {
                    // TODO: Check so that exactly one device is selected.
                    selected = responder.getDevices().get(0);
                    output.writeBytes("OKAY");
                }
                else if ("host:devices".equals(command)) {
                    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                    DataOutputStream writer = new DataOutputStream(tmp);
                    for (AdbDeviceResponder d : responder.getDevices())
                    {
                        writer.writeBytes(d.getSerial() + "\t" + d.getType() + "\n");
                    }
                    output.writeBytes("OKAY");
                    send(output, new String(tmp.toByteArray(), Charset.forName("utf-8")));
                }
                else if (command.startsWith("host:transport:"))
                {
                    String serial = command.substring("host:transport:".length());
                    selected = findDevice(serial);
                    output.writeBytes("OKAY");
                }
                else if ("sync:".equals(command)) {
                    output.writeBytes("OKAY");
                    try
                    {
                        sync(output, input);
                    }
                    catch (JadbException e) { // sync response with a different type of fail message
                        SyncTransport sync = new SyncTransport(output, input);
                        sync.send("FAIL", e.getMessage());
                    }
                }
                else
                {
                    throw new ProtocolException("Unknown command: " + command);
                }
            } catch (ProtocolException e) {
                output.writeBytes("FAIL");
                send(output, e.getMessage());
            }
            output.flush();
        }
    }

    private int readInt(DataInput input) throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    private String readString(DataInput input, int length) throws IOException {
        byte[] responseBuffer = new byte[length];
        input.readFully(responseBuffer);
        return new String(responseBuffer, Charset.forName("utf-8"));
    }

    private void sync(DataOutput output, DataInput input) throws IOException, JadbException {
        String id = readString(input, 4);
        int length = readInt(input);
        if ("SEND".equals(id))
        {
            String remotePath = readString(input, length);
            int idx = remotePath.lastIndexOf(',');
            String path = remotePath;
            int mode = 0666;
            if (idx > 0)
            {
                path = remotePath.substring(0, idx);
                mode = Integer.parseInt(remotePath.substring(idx + 1));
            }
            SyncTransport transport = new SyncTransport(output, input);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            transport.readChunksTo(buffer);
            selected.filePushed(new RemoteFile(path), mode, buffer);
            transport.sendStatus("OKAY", 0); // 0 = ignored
        }
        else if ("RECV".equals(id)) {
            String remotePath = readString(input, length);
            SyncTransport transport = new SyncTransport(output, input);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            selected.filePulled(new RemoteFile(remotePath), buffer);
            transport.sendStream(new ByteArrayInputStream(buffer.toByteArray()));
            transport.sendStatus("DONE", 0); // ignored
        }
        else throw new JadbException("Unknown sync id " + id);
    }

    private String getCommandLength(String command) {
		return String.format("%04x", command.length());
	}
	
	public void send(DataOutput writer, String response) throws IOException {
		writer.writeBytes(getCommandLength(response));
		writer.writeBytes(response);
	}
}