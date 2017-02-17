package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: pablobaxter.
 */
public class BatteryManager {

    private final JadbDevice device;

    public BatteryManager(JadbDevice device) {
        this.device = device;
    }

    public void simulateUsbUnplug() throws IOException, JadbException {
        try (InputStream stream = device.executeShell("dumpsys", "battery", "unplug")) {
            Stream.flushRead(stream);
        }
    }

    public void resetUsbPlug() throws IOException, JadbException {
        try (InputStream stream = device.executeShell("dumpsys", "battery", "reset")) {
            Stream.flushRead(stream);
        }
    }

}
