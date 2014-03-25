package se.vidstige.jadb.server;

import java.io.ByteArrayOutputStream;

/**
 * Created by vidstige on 20/03/14.
 */
public interface AdbDeviceResponder {
    String getSerial();
    String getType();

    void filePushed(String path, int mode, ByteArrayOutputStream buffer);
}
