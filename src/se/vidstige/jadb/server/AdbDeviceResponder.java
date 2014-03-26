package se.vidstige.jadb.server;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.ByteArrayOutputStream;

/**
 * Created by vidstige on 20/03/14.
 */
public interface AdbDeviceResponder {
    String getSerial();
    String getType();

    void filePushed(RemoteFile path, int mode, ByteArrayOutputStream buffer) throws JadbException;
}
