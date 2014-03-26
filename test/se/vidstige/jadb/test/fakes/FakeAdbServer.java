package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.server.AdbDeviceResponder;
import se.vidstige.jadb.server.AdbResponder;
import se.vidstige.jadb.server.AdbServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vidstige on 2014-03-20.
 */
public class FakeAdbServer implements AdbResponder {
    private final AdbServer server;
    private List<AdbDeviceResponder> devices = new ArrayList<AdbDeviceResponder>();

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
        org.junit.Assert.assertEquals(0, remoteFileExpectations.size());
    }

    private static class RemoteFileExpectation {

        private final String serial;
        private final RemoteFile path;
        private final byte[] contents;

        public RemoteFileExpectation(String serial, RemoteFile path, byte[] contents) {

            this.serial = serial;
            this.path = path;
            this.contents = contents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RemoteFileExpectation that = (RemoteFileExpectation) o;

            if (!Arrays.equals(contents, that.contents)) return false;
            if (!path.equals(that.path)) return false;
            if (serial != null ? !serial.equals(that.serial) : that.serial != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = serial != null ? serial.hashCode() : 0;
            result = 31 * result + path.hashCode();
            result = 31 * result + Arrays.hashCode(contents);
            return result;
        }
    }

    private List<RemoteFileExpectation> remoteFileExpectations = new ArrayList<RemoteFileExpectation>();

    public void expectPush(String serial, RemoteFile path, String contents){
        expectPush(serial, path, contents.getBytes(Charset.forName("UTF-8")));
    }

    public void expectPush(String serial, RemoteFile path, byte[] contents)
    {
        remoteFileExpectations.add(new RemoteFileExpectation(serial, path, contents));
    }

    private void filePushed(String serial, RemoteFile path, byte[] contents) {
        boolean removed = remoteFileExpectations.remove(new RemoteFileExpectation(serial, path, contents));
        if (!removed) throw new RuntimeException("Unexpected push to device " + serial + " at " + path.getPath());

    }

    @Override
    public List<AdbDeviceResponder> getDevices() {
        return devices;
    }

    private class DeviceResponder implements AdbDeviceResponder {
        private final String serial;

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
        public void filePushed(String path, int mode, ByteArrayOutputStream buffer) {
            FakeAdbServer.this.filePushed(serial, new RemoteFile(path), buffer.toByteArray());
        }
    }
}
