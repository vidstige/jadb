package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.server.AdbDeviceResponder;
import se.vidstige.jadb.server.AdbResponder;
import se.vidstige.jadb.server.AdbServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidstige on 2014-03-20.
 */
public class FakeAdbServer implements AdbResponder {
    private final AdbServer server;
    private List<DeviceResponder> devices = new ArrayList<DeviceResponder>();

    public FakeAdbServer(int port) {
        server = new AdbServer(this, port);
    }


    public void start() throws InterruptedException {
        server.start();
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
    }

    @Override
    public void onCommand(String command) {
        System.out.println("command: " +command);
    }

    @Override
    public int getVersion() {
        return 31;
    }

    public void add(String serial)
    {
        devices.add(new DeviceResponder(serial));
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

    public ExpectationBuilder expectPush(String serial, RemoteFile path)
    {
        return findBySerial(serial).expectPush(path);
    }

    public ExpectationBuilder expectPull(String serial, RemoteFile path) {
        return findBySerial(serial).expectPull(path);
    }

    @Override
    public List<AdbDeviceResponder> getDevices() {
        return new ArrayList<AdbDeviceResponder>(devices);
    }

    private class DeviceResponder implements AdbDeviceResponder {
        private final String serial;
        private List<FileExpectation> expectations = new ArrayList<FileExpectation>();

        private DeviceResponder(String serial) {
            this.serial = serial;
        }

        @Override
        public String getSerial() {
            return serial;
        }

        @Override
        public String getType() {
            return "device";
        }

        @Override
        public void filePushed(RemoteFile path, int mode, ByteArrayOutputStream buffer) throws JadbException {
            for (FileExpectation fe : expectations) {
                if (fe.matches(path))
                {
                    expectations.remove(fe);
                    fe.throwIfFail();
                    fe.verifyContent(buffer.toByteArray());
                    return;
                }
            }
            throw new JadbException("Unexpected push to device " + serial + " at " + path);
        }

        @Override
        public void filePulled(RemoteFile path, ByteArrayOutputStream buffer) throws JadbException, IOException {
            for (FileExpectation fe : expectations) {
                if (fe.matches(path))
                {
                    expectations.remove(fe);
                    fe.throwIfFail();
                    fe.returnFile(buffer);
                    return;
                }
            }
            throw new JadbException("Unexpected push to device " + serial + " at " + path);
        }

        public void verifyExpectations() {
            org.junit.Assert.assertEquals(0, expectations.size());
        }

        private class FileExpectation implements ExpectationBuilder {
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
                this.content = content.getBytes(Charset.forName("utf-8"));
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

        public ExpectationBuilder expectPush(RemoteFile path) {
            FileExpectation expectation = new FileExpectation(path);
            expectations.add(expectation);
            return expectation;
        }

        public ExpectationBuilder expectPull(RemoteFile path) {
            FileExpectation expectation = new FileExpectation(path);
            expectations.add(expectation);
            return expectation;
        }

    }
}
