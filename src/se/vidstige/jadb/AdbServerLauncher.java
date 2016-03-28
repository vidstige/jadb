package se.vidstige.jadb;

import java.io.IOException;

/**
 * Launches the ADB server
 */
public class AdbServerLauncher {
    private Runtime runtime;

    public AdbServerLauncher() {
        this(Runtime.getRuntime());
    }

    public AdbServerLauncher(Runtime runtime) {
        this.runtime = runtime;
    }

    private String findAdbExecutable() {
        String android_home = System.getenv("ANDROID_HOME");
        if (android_home == null || android_home.equals("")) {
            return "adb";
        }

        return android_home + "/platform-tools/adb";
    }

    public void launch() throws IOException, InterruptedException {
        Process p = runtime.exec(new String[]{findAdbExecutable(), "start-server"});
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue != 0) throw new IOException("adb exited with exit code: " + exitValue);
    }
}
