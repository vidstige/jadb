package se.vidstige.jadb;

import java.io.*;

public class SyncTransport extends RawSyncTransport implements Closeable {
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    public SyncTransport(OutputStream outputStream, InputStream inputStream) {
        this(new DataOutputStream(outputStream), new DataInputStream(inputStream));
    }

    private SyncTransport(DataOutputStream outputStream, DataInputStream inputStream) {
        super(outputStream, inputStream);
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
    }
}
