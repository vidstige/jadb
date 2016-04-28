package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.Stream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Java interface to package manager. Launches package manager through jadb
 */
public class PackageManager {
    private final JadbDevice device;

    public PackageManager(JadbDevice device) {
        this.device = device;
    }

    public List<Package> getPackages() throws IOException, JadbException {
        ArrayList<Package> result = new ArrayList<Package>();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(device.executeShell("pm", "list", "packages"), Charset.forName("UTF-8")));
            String line;
            while ((line = input.readLine()) != null) {
                final String prefix = "package:";
                if (line.startsWith(prefix)) {
                    result.add(new Package(line.substring(prefix.length())));
                }
            }
        } finally {
            if (input != null) input.close();
        }
        return result;
    }

    private String getErrorMessage(String operation, String target, String errorMessage) {
        return "Could not " + operation + " " + target + ": " + errorMessage;
    }

    private void verifyOperation(String operation, String target, String result) throws JadbException {
        if (!result.contains("Success")) throw new JadbException(getErrorMessage(operation, target, result));
    }

    public void install(File apkFile) throws IOException, JadbException {
        RemoteFile remote = new RemoteFile("/sdcard/tmp/" + apkFile.getName());
        device.push(apkFile, remote);
        InputStream s = device.executeShell("pm", "install", Bash.quote(remote.getPath()));
        String result = Stream.readAll(s, Charset.forName("UTF-8"));
        // TODO: Remove remote file
        verifyOperation("install", apkFile.getName(), result);
    }

    public void uninstall(Package name) throws IOException, JadbException {
        InputStream s = device.executeShell("pm", "uninstall", name.toString());
        String result = Stream.readAll(s, Charset.forName("UTF-8"));
        verifyOperation("uninstall", name.toString(), result);
    }
}
