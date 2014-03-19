package se.vidstige.jadb;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by vidstige on 2014-03-19.
 */
class SyncTransport {

    private final DataOutputStream output;
    private final DataInputStream input;

    public SyncTransport(OutputStream outputStream, InputStream inputStream) {
        output = new DataOutputStream(outputStream);
        input = new DataInputStream(inputStream);
    }

    public void send(String syncCommand, String name) throws IOException {
        if (syncCommand.length() != 4) throw new IllegalArgumentException("sync commands must have length 4");
        output.writeBytes(syncCommand);
        output.writeInt(Integer.reverseBytes(name.length()));
        output.writeBytes(name);
    }

    private int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    private String readString(int length) throws IOException {
        byte[] buffer = new byte[length];
        input.read(buffer);
        return new String(buffer, Charset.forName("utf-8"));
    }

    public RemoteFile readDirectoryEntry() throws IOException {
        String id = readString(4);
        int mode = readInt();
        int size = readInt();
        int time = readInt();
        int nameLenght = readInt();
        String name = readString(nameLenght);

        if ("DENT".equals(id) == false) return RemoteFile.DONE;
        return new RemoteFile(id, name, mode, size, time);
    }
}
