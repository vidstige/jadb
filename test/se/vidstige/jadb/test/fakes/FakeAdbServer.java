package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.server.AdbDeviceResponder;
import se.vidstige.jadb.server.AdbResponder;
import se.vidstige.jadb.server.AdbServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by vidstige on 2014-03-20.
 */
public class FakeAdbServer implements AdbResponder {
    private final AdbServer server;
    private final List<DeviceResponder> devices = new ArrayList<>();

    public FakeAdbServer(int port) {
        server = new AdbServer(this, port);
    }

    public void start() throws InterruptedException {
        System.out.println("Starting fake on port " + server.getPort());
        server.start();
    }

    public void stop() throws IOException, InterruptedException {
        System.out.println("Stopping fake on port " + server.getPort());
        server.stop();
    }

    @Override
    public void onCommand(String command) {
        System.out.println("command: " + command);
    }

    @Override
    public int getVersion() {
        return 31;
    }

    public void add(String serial) {
        devices.add(new DeviceResponder(serial, "device"));
    }

    public void add(String serial, String type) {
        devices.add(new DeviceResponder(serial, type));
    }

    public void verifyExpectations() {
        for (DeviceResponder d : devices)
            d.verifyExpectations();
    }

    public interface ExpectationBuilder {
        void failWith(String message);

        void withContent(byte[] content);

        void withContent(String content);
    }

    private DeviceResponder findBySerial(String serial) {
        for (DeviceResponder d : devices) {
            if (d.getSerial().equals(serial)) return d;
        }
        return null;
    }

    public ExpectationBuilder expectPush(String serial, RemoteFile path) {
        return findBySerial(serial).expectPush(path);
    }

    public ExpectationBuilder expectPull(String serial, RemoteFile path) {
        return findBySerial(serial).expectPull(path);
    }

    public DeviceResponder.ShellExpectation expectShell(String serial, String commands) {
        return findBySerial(serial).expectShell(commands);
    }

    public void expectTcpip(String serial, Integer port) {
        findBySerial(serial).expectTcpip(port);
    }

    public DeviceResponder.ListExpectation expectList(String serial, String remotePath) {
        return findBySerial(serial).expectList(remotePath);
    }

    @Override
    public List<AdbDeviceResponder> getDevices() {
        return new ArrayList<AdbDeviceResponder>(devices);
    }

    private static class DeviceResponder implements AdbDeviceResponder {
        private final String serial;
        private final String type;
        private List<FileExpectation> fileExpectations = new ArrayList<>();
        private List<ShellExpectation> shellExpectations = new ArrayList<>();
        private List<ListExpectation> listExpectations = new ArrayList<>();
        private List<Integer> tcpipExpectations = new ArrayList<>();

        private DeviceResponder(String serial, String type) {
            this.serial = serial;
            this.type = type;
        }

        @Override
        public String getSerial() {
            return serial;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public void filePushed(RemoteFile path, int mode, ByteArrayOutputStream buffer) throws JadbException {
            for (FileExpectation fe : fileExpectations) {
                if (fe.matches(path)) {
                    fileExpectations.remove(fe);
                    fe.throwIfFail();
                    fe.verifyContent(buffer.toByteArray());
                    return;
                }
            }
            throw new JadbException("Unexpected push to device " + serial + " at " + path);
        }

        @Override
        public void filePulled(RemoteFile path, ByteArrayOutputStream buffer) throws JadbException, IOException {
            for (FileExpectation fe : fileExpectations) {
                if (fe.matches(path)) {
                    fileExpectations.remove(fe);
                    fe.throwIfFail();
                    fe.returnFile(buffer);
                    return;
                }
            }
            throw new JadbException("Unexpected push to device " + serial + " at " + path);
        }

        @Override
        public void shell(String command, DataOutputStream stdout, DataInput stdin) throws IOException {
            for (ShellExpectation se : shellExpectations) {
                if (se.matches(command)) {
                    shellExpectations.remove(se);
                    se.writeOutputTo(stdout);
                    return;
                }
            }
            throw new ProtocolException("Unexpected shell to device " + serial + ": " + command);
        }

        @Override
        public void enableIpCommand(String port, DataOutputStream outputStream) throws IOException {
            for (Integer expectation : tcpipExpectations) {
                if (expectation == Integer.parseInt(port)) {
                    tcpipExpectations.remove(expectation);
                    return;
                }
            }

            throw new ProtocolException("Unexpected tcpip to device " + serial + ": (port) " + port);

        }

        @Override
        public List<RemoteFile> list(String path) throws IOException {
            for (ListExpectation le : listExpectations) {
                if (le.matches(path)) {
                    listExpectations.remove(le);
                    return le.getFiles();
                }
            }
            throw new ProtocolException("Unexpected list of device " + serial + " in dir " + path);
        }

        public void verifyExpectations() {
            org.junit.Assert.assertEquals(0, fileExpectations.size());
            org.junit.Assert.assertEquals(0, shellExpectations.size());
            org.junit.Assert.assertEquals(0, listExpectations.size());
            org.junit.Assert.assertEquals(0, tcpipExpectations.size());
        }

        private static class FileExpectation implements ExpectationBuilder {
            private final RemoteFile path;
            private byte[] content;
            private String failMessage;

            public FileExpectation(RemoteFile path) {
                this.path = path;
                content = null;
                failMessage = null;
            }

            @Override
            public void failWith(String message) {
                failMessage = message;
            }

            @Override
            public void withContent(byte[] content) {
                this.content = content;
            }

            @Override
            public void withContent(String content) {
                this.content = content.getBytes(StandardCharsets.UTF_8);
            }

            public boolean matches(RemoteFile path) {
                return this.path.equals(path);
            }

            public void throwIfFail() throws JadbException {
                if (failMessage != null) throw new JadbException(failMessage);
            }

            public void verifyContent(byte[] content) {
                org.junit.Assert.assertArrayEquals(this.content, content);
            }

            public void returnFile(ByteArrayOutputStream buffer) throws IOException {
                buffer.write(content);
            }
        }

        public static class ShellExpectation {
            private final String command;
            private byte[] stdout;

            public ShellExpectation(String command) {
                this.command = command;
            }

            public boolean matches(String command) {
                return command.equals(this.command);
            }

            public void returns(String stdout) {
                this.stdout = stdout.getBytes(StandardCharsets.UTF_8);
            }

            public void writeOutputTo(DataOutputStream stdout) throws IOException {
                stdout.write(this.stdout);
            }
        }

        public static class ListExpectation {

            private final String remotePath;
            private final List<RemoteFile> files = new ArrayList<>();

            public ListExpectation(String remotePath) {
                this.remotePath = remotePath;
            }

            public boolean matches(String remotePath) {
                return remotePath.equals(this.remotePath);
            }

            public ListExpectation withFile(String path, int size, int modifyTime) {
                files.add(new MockFileEntry(path, size, modifyTime, false));
                return this;
            }

            public ListExpectation withDir(String path, int modifyTime) {
                files.add(new MockFileEntry(path, -1, modifyTime, true));
                return this;
            }

            public List<RemoteFile> getFiles() {
                return Collections.unmodifiableList(files);
            }

            private static class MockFileEntry extends RemoteFile {

                private final int size;
                private final int modifyTime;
                private final boolean dir;

                MockFileEntry(String path, int size, int modifyTime, boolean dir) {
                    super(path);
                    this.size = size;
                    this.modifyTime = modifyTime;
                    this.dir = dir;
                }

                public int getSize() {
                    return size;
                }

                public int getLastModified() {
                    return modifyTime;
                }

                public boolean isDirectory() {
                    return dir;
                }

            }

        }

        public ExpectationBuilder expectPush(RemoteFile path) {
            FileExpectation expectation = new FileExpectation(path);
            fileExpectations.add(expectation);
            return expectation;
        }

        public ExpectationBuilder expectPull(RemoteFile path) {
            FileExpectation expectation = new FileExpectation(path);
            fileExpectations.add(expectation);
            return expectation;
        }

        public ShellExpectation expectShell(String command) {
            ShellExpectation expectation = new ShellExpectation(command);
            shellExpectations.add(expectation);
            return expectation;
        }

        public ListExpectation expectList(String remotePath) {
            ListExpectation expectation = new ListExpectation(remotePath);
            listExpectations.add(expectation);
            return expectation;
        }

        public void expectTcpip(int port) {
            tcpipExpectations.add(port);
        }
    }
}
