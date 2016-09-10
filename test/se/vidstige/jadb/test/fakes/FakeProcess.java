package se.vidstige.jadb.test.fakes;

import java.io.InputStream;
import java.io.OutputStream;

public class FakeProcess extends Process {
    private final int exitValue;

    public FakeProcess(int exitValue) {
        this.exitValue = exitValue;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return exitValue;
    }

    @Override
    public void destroy() {
    }
}
