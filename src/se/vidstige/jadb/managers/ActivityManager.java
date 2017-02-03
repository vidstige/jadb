package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: pablobaxter.
 */
public class ActivityManager {

    private final JadbDevice device;

    public ActivityManager(JadbDevice device) {
        this.device = device;
    }

    public void broadcastIntent(Intent intent, String permission) throws IOException, JadbException {
        List<String> args = new ArrayList<>();
        args.add("broadcast");
        intent.generate(args);
        if(permission != null) {
            args.add("--receiver-permission");
            args.add(permission);
        }
        try (InputStream stream = device.executeShell("am", args.toArray(new String[args.size()]))) {
            Stream.flushRead(stream);
            stream.close();
        }
    }

    public void broadcastIntent(Intent intent) throws IOException, JadbException {
        broadcastIntent(intent, null);
    }
}
