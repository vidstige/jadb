package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.Stream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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

    public void remove(RemoteFile file) throws IOException, JadbException {
        InputStream s = device.executeShell("rm", "-f", Bash.quote(file.getPath()));
        Stream.readAll(s, Charset.forName("UTF-8"));
    }

    private void install(File apkFile, List<String> extraArguments) throws IOException, JadbException {
        RemoteFile remote = new RemoteFile("/sdcard/tmp/" + apkFile.getName());
        device.push(apkFile, remote);
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("install");
        arguments.addAll(extraArguments);
        arguments.add(remote.getPath());
        InputStream s = device.executeShell("pm", arguments.toArray(new String[arguments.size()]));
        String result = Stream.readAll(s, Charset.forName("UTF-8"));
        remove(remote);
        verifyOperation("install", apkFile.getName(), result);
    }

    public void install(File apkFile) throws IOException, JadbException {
        install(apkFile, new ArrayList<String>(0));
    }

    public void installWithOptions(File apkFile, List<? extends InstallOption> options) throws IOException, JadbException {
        List<String> optionsAsStr = new ArrayList<>(options.size());

        for(InstallOption installOption : options) {
            optionsAsStr.add(installOption.getStringRepresentation());
        }
        install(apkFile, optionsAsStr);
    }

    public void forceInstall(File apkFile) throws IOException, JadbException {
        installWithOptions(apkFile, Collections.singletonList(REINSTALL_KEEPING_DATA));
    }

    public void uninstall(Package name) throws IOException, JadbException {
        InputStream s = device.executeShell("pm", "uninstall", name.toString());
        String result = Stream.readAll(s, Charset.forName("UTF-8"));
        verifyOperation("uninstall", name.toString(), result);
    }

    public void launch(Package name) throws IOException, JadbException {
        InputStream s = device.executeShell("monkey", "-p", name.toString(), "-c", "android.intent.category.LAUNCHER", "1");
    }

    //<editor-fold desc="InstallOption">
    public static class InstallOption {
        InstallOption(String ... varargs) {
            for(String str: varargs) {
                stringBuilder.append(str).append(" ");
            }
        }

        private final StringBuilder stringBuilder = new StringBuilder();

        private String getStringRepresentation() {
            return stringBuilder.toString();
        }
    }

    public static final InstallOption WITH_FORWARD_LOCK = new InstallOption("-l");

    public static final InstallOption REINSTALL_KEEPING_DATA =
            new InstallOption("-r");

    public static final InstallOption ALLOW_TEST_APK =
            new InstallOption("-t");

    public static InstallOption WITH_INSTALLER_PACKAGE_NAME(String name)
    {
        return new InstallOption("-t", name);
    }

    public static InstallOption ON_SHARED_MASS_STORAGE(String name) {
        return new InstallOption("-s", name);
    }

    public static InstallOption ON_INTERNAL_SYSTEM_MEMORY(String name) {
        return new InstallOption("-f", name);
    }

    public static final InstallOption ALLOW_VERSION_DOWNGRADE =
            new InstallOption("-d");

    /**
     * This option is sSupported only from Android 6.X+
     */
    public static final InstallOption GRANT_ALL_PERMISSIONS = new InstallOption("-g");

    //</editor-fold>
}
