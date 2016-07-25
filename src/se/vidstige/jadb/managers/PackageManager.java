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

    public void installWithOptions(File apkFile, List<? extends InstallOptions> options) throws IOException, JadbException {
        List<String> optionsAsStr = new ArrayList<>(options.size());

        for(InstallOptions installOptions: options) {
            optionsAsStr.add(installOptions.getStringRepresentation());
        }
        install(apkFile, optionsAsStr);
    }

    public void forceInstall(File apkFile) throws IOException, JadbException {
        installWithOptions(apkFile, Collections.singletonList(new REINSTALL_KEEPING_DATA()));
    }

    public void uninstall(Package name) throws IOException, JadbException {
        InputStream s = device.executeShell("pm", "uninstall", name.toString());
        String result = Stream.readAll(s, Charset.forName("UTF-8"));
        verifyOperation("uninstall", name.toString(), result);
    }

    public void launch(Package name) throws IOException, JadbException {
        InputStream s = device.executeShell("monkey", "-p", name.toString(), "-c", "android.intent.category.LAUNCHER", "1");
    }

    //<editor-fold desc="InstallOptions">
    public static abstract class InstallOptions {
        InstallOptions(String ... varargs) {
            for(String str: varargs) {
                stringBuilder.append(str).append(" ");
            }
        }

        private final StringBuilder stringBuilder = new StringBuilder();

        private String getStringRepresentation() {
            return stringBuilder.toString();
        }
    }


    public static class WITH_FORWARD_LOCK extends InstallOptions {
        public WITH_FORWARD_LOCK() {
            super("-l");
        }
    }

    public static class REINSTALL_KEEPING_DATA extends InstallOptions {
        public REINSTALL_KEEPING_DATA() {
            super("-r");
        }
    }

    public static final class ALLOW_TEST_APK extends InstallOptions {
        public ALLOW_TEST_APK() {
            super("-t");
        }
    }

    public static final class WITH_INSTALLER_PACKAGE_NAME extends InstallOptions {
        public WITH_INSTALLER_PACKAGE_NAME(String name) {
            super("-t", name);
        }
    }

    public static final class ON_SHARED_MASS_STORAGE extends InstallOptions {
        public ON_SHARED_MASS_STORAGE(String name) {
            super("-s", name);
        }
    }

    public static final class ON_INTERNAL_SYSTEM_MEMORY extends InstallOptions {
        public ON_INTERNAL_SYSTEM_MEMORY(String name) {
            super("-f", name);
        }
    }

    public static final class ALLOW_VERSION_DOWNGRADE extends InstallOptions {
        public ALLOW_VERSION_DOWNGRADE() {
            super("-d");
        }
    }

    /**
     * This option is sSupported only from Android 6.X+
     */
    public static final class GRANT_ALL_PERMISSIONS extends InstallOptions {
        public GRANT_ALL_PERMISSIONS() {
            super("-g");
        }
    }

    public static final class CUSTOM_PARAMETER extends InstallOptions {
        public CUSTOM_PARAMETER(String ... varargs) {
            super(varargs);
        }
    }
    //</editor-fold>
}
