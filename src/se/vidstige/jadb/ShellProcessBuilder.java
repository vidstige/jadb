package se.vidstige.jadb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;


/**
 * A builder of a {@link Process} corresponding to an ADB shell command.
 *
 * <p>This builder allows for configuration of the {@link Process}'s output and error streams as well as the
 * {@link Executor} to use when starting the shell process. The output and error streams may be either be redirected
 * (using {@link java.lang.ProcessBuilder.Redirect}) or given an explicit {@link OutputStream}. You may also combine
 * the output and error streams via {@link #redirectErrorStream(boolean) redirectErrorStream(true)}.</p>
 *
 * <p>Use {@link #start()} to execute the command, and then use {@link Process#waitFor()} to wait for the command to
 * complete.</p>
 *
 * <p><b>Warning:</b> If stdout and stderr are both set to {@link java.lang.ProcessBuilder.Redirect#PIPE} (the default),
 * you <b>must</b> read from their InputStreams ({@link Process#getInputStream()} and {@link Process#getErrorStream()})
 * <b>concurrently</b>. This requires having two separate threads to read the input streams separately. Otherwise,
 * the process may deadlock. To avoid using threads, you can use {@link #redirectErrorStream(boolean)}, in which case
 * you must read all output from {@link Process#getInputStream()} before calling {@link Process#waitFor()}:
 *
 * <pre>{@code
 *   Process process = jadbDevice.shellProcessBuilder("command")
 *       .redirectErrorStream(errorStream)
 *       .start();
 *   String stdoutAndStderr = new Scanner(process.getInputStream()).useDelimiter("\\A").next();
 *   int exitCode = process.waitFor();
 * }</pre>
 * <p>
 * You can also use one of the {@code redirectOutput} methods to have the output automatically redirected. For example,
 * to buffer all of stdout and stderr separately, you can use {@link java.io.ByteArrayOutputStream}:
 *
 * <pre>{@code
 *   ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 *   ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
 *   Process process = jadbDevice.shellProcessBuilder("command")
 *       .redirectOutput(outputStream)
 *       .redirectError(errorStream)
 *       .start();
 *   int exitCode = process.waitFor();
 *   String stdout = outputStream.toString(StandardCharsets.UTF_8.name());
 *   String stderr = errorStream.toString(StandardCharsets.UTF_8.name());
 * }</pre>
 */
public class ShellProcessBuilder {

    private JadbDevice device;
    private String command;
    private ProcessBuilder.Redirect outRedirect = ProcessBuilder.Redirect.PIPE;
    private OutputStream outOs = null;
    private ProcessBuilder.Redirect errRedirect = ProcessBuilder.Redirect.PIPE;
    private OutputStream errOs = null;
    private boolean redirectErrorStream;
    private Executor executor = null;

    ShellProcessBuilder(JadbDevice device, String command) {
        this.device = device;
        this.command = command;
    }

    private void checkValidForWrite(ProcessBuilder.Redirect destination) {
        if (destination.type() == ProcessBuilder.Redirect.Type.READ) {
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        }
    }

    /**
     * Redirect stdout to the given destination. If set to anything other than
     * {@link java.lang.ProcessBuilder.Redirect#PIPE} (the default), {@link Process#getInputStream()} does nothing.
     *
     * @param destination where to redirect
     * @return this
     */
    public ShellProcessBuilder redirectOutput(ProcessBuilder.Redirect destination) {
        checkValidForWrite(destination);
        outRedirect = destination;
        outOs = null;
        return this;
    }

    /**
     * Redirect stdout directly to the given output stream.
     * <p>Note: this output steam will be called from a separate thread.</p>
     *
     * @param destination OutputStream to write
     * @return this
     */
    public ShellProcessBuilder redirectOutput(OutputStream destination) {
        outRedirect = null;
        outOs = destination;
        return this;
    }

    /**
     * Redirect stderr to the given destination. If set to anything other than
     * {@link java.lang.ProcessBuilder.Redirect#PIPE} (the default), {@link Process#getErrorStream()} does nothing.
     *
     * @param destination where to redirect
     * @return this
     */
    public ShellProcessBuilder redirectError(ProcessBuilder.Redirect destination) {
        checkValidForWrite(destination);
        errRedirect = destination;
        errOs = null;
        return this;
    }

    /**
     * Redirect stderr directly to the given output stream.
     * <p>Note: this output steam will be called from a separate thread.</p>
     *
     * @param destination OutputStream to write
     * @return this
     */
    public ShellProcessBuilder redirectError(OutputStream destination) {
        errRedirect = null;
        errOs = destination;
        return this;
    }

    /**
     * Set redirecting of the error stream directly to the output stream. If set, any {@code redirectError} calls are
     * ignored, and the returned Process
     *
     * @param redirectErrorStream true to enable redirecting of the error stream
     * @return this
     */
    public ShellProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * Set the {@link Executor} to use to run the process handling thread. If not set, uses
     * {@link Executors#newSingleThreadExecutor()}.
     *
     * @param executor An executor
     * @return this
     */
    public ShellProcessBuilder useExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Starts the shell command.
     *
     * @return a {@link Process}
     * @throws IOException
     * @throws JadbException
     */
    public ShellProcess start() throws IOException, JadbException {
        Transport transport = null;
        try {
            final OutputStream outOs = getOutputStream(this.outOs, this.outRedirect, System.out);
            InputStream outIs = getConnectedPipe(outOs);

            final OutputStream errOs;
            InputStream errIs;
            if (redirectErrorStream) {
                errOs = outOs;
                errIs = NullInputStream.INSTANCE;
            } else {
                errOs = getOutputStream(this.errOs, this.errRedirect, System.err);
                errIs = getConnectedPipe(errOs);
            }

            transport = device.getTransport();
            final ShellProtocolTransport shellProtocolTransport = transport.startShellProtocol(this.command);
            OutputStream inOs = shellProtocolTransport.getOutputStream();

            FutureTask<Integer> transportTask = new FutureTask<>(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    try (ShellProtocolTransport unused1 = shellProtocolTransport;
                         OutputStream unused2 = outOs;
                         OutputStream unused3 = errOs) {
                        return shellProtocolTransport.demuxOutput(outOs, errOs);
                    }
                }
            });

            if (executor == null) {
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.execute(transportTask);
                service.shutdown();
            } else {
                executor.execute(transportTask);
            }

            return new ShellProcess(inOs, outIs, errIs, transportTask);
        } catch (IOException | JadbException | RuntimeException e) {
            if (transport != null) {
                transport.close();
            }
            throw e;
        }
    }

    private OutputStream getOutputStream(OutputStream os, ProcessBuilder.Redirect destination, OutputStream inherit) throws IOException {
        if (os != null) {
            return os;
        }
        switch (destination.type()) {
            case PIPE:
                return new PipedOutputStream();
            case INHERIT:
                return inherit;
            case READ:
                throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
            case WRITE:
                return Files.newOutputStream(destination.file().toPath());
            case APPEND:
                return Files.newOutputStream(destination.file().toPath(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            default:
                throw new IllegalArgumentException("Unknown redirect type: " + destination);
        }
    }

    private InputStream getConnectedPipe(OutputStream os) throws IOException {
        if (os instanceof PipedOutputStream) {
            return new PipedInputStream((PipedOutputStream) os);
        }
        return NullInputStream.INSTANCE;
    }

    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = new NullInputStream();

        private NullInputStream() {
        }

        public int read() {
            return -1;
        }

        public int available() {
            return 0;
        }
    }
}
