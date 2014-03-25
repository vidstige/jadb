package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.server.AdbDeviceResponder;
import se.vidstige.jadb.server.AdbResponder;
import se.vidstige.jadb.server.AdbServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    @Override
    public List<AdbDeviceResponder> getDevices() {
        return devices;
    }

    private static class DeviceResponder implements AdbDeviceResponder {
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

        }
    }
}
