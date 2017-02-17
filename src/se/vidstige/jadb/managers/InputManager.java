package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: pablobaxter.
 */
public class InputManager {

    private final JadbDevice device;

    public InputManager(JadbDevice device) {
        this.device = device;
    }

    public void sendEvent(InputEvent event) throws JadbException {
        try (InputStream is = device.executeShell("input", event.buildArgs())) {
            Stream.flushRead(is);
            is.close();
        } catch (IOException e) {
            throw new JadbException(e.getMessage());
        }
    }
}
