package se.vidstige.jadb;

import java.io.IOException;
import java.util.Map;

/**
 * Launches the ADB server
 */
public class AdbServerLauncher {
    private final String executable;
    private Subprocess subprocess;

    public AdbServerLauncher(Subprocess subprocess, Map<String, String> environment) {
        this.subprocess = subprocess;
        this.executable = findAdbExecutable(environment);
    }

    private static String findAdbExecutable(Map<String, String> environment) {
        String android_home = environment.get("ANDROID_HOME");
        if (android_home == null || android_home.equals("")) {
            return "adb";
        }
        return android_home + "/platform-tools/adb";
    }

    public void launch() throws IOException, InterruptedException {
        Process p = subprocess.execute(new String[]{executable, "start-server"});
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue != 0) throw new IOException("adb exited with exit code: " + exitValue);
    }
}
