package se.vidstige.jadb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ShellProcess extends Process {

    private static final int KILLED_STATUS_CODE = 9;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final InputStream errorStream;
    private final Future<Integer> exitCodeFuture;
    private Integer exitCode = null;
    private final ShellProtocolTransport transport;

    ShellProcess(OutputStream outputStream, InputStream inputStream, InputStream errorStream,
                 Future<Integer> exitCodeFuture, ShellProtocolTransport transport) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.errorStream = errorStream;
        this.exitCodeFuture = exitCodeFuture;
        this.transport = transport;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
        if (exitCode == null) {
            try {
                exitCode = exitCodeFuture.get();
            } catch (CancellationException e) {
                exitCode = KILLED_STATUS_CODE;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return exitCode;
    }

    /* For 1.8 */
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        if (exitCode == null) {
            try {
                exitCode = exitCodeFuture.get(timeout, unit);
            } catch (CancellationException e) {
                exitCode = KILLED_STATUS_CODE;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int exitValue() {
        if (exitCode != null) {
            return exitCode;
        }
        if (exitCodeFuture.isDone()) {
            try {
                exitCode = exitCodeFuture.get(0, TimeUnit.SECONDS);
                return exitCode;
            } catch (CancellationException e) {
                exitCode = KILLED_STATUS_CODE;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                // fallthrough, but should never happen
            } catch (InterruptedException e) {
                // fallthrough, but should never happen
                Thread.currentThread().interrupt();
            }
        }
        throw new IllegalThreadStateException();
    }

    @Override
    public void destroy() {
        if (isAlive()) {
            try {
                exitCodeFuture.cancel(true);
                // interrupt (usually) doesn't work for blocking read -- this will cause SocketException
                transport.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* For 1.8 */
    public boolean isAlive() {
        return !exitCodeFuture.isDone();
    }
}
