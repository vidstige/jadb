package se.vidstige.jadb;

import java.io.IOException;
import java.util.Map;

public class AdbServerController {
    private final String executable;
    private Subprocess subprocess;

    /**
     * Creates a new controller loading ADB from the environment.
     *
     * @param subprocess  the sub-process.
     * @param environment the environment to use to locate the ADB executable.
     */
    public AdbServerController(Subprocess subprocess, Map<String, String> environment) {
        this(subprocess, findAdbExecutable(environment));
    }

    /**
     * Creates a new controller with the specified ADB.
     *
     * @param subprocess the sub-process.
     * @param executable the location of the ADB executable.
     */
    public AdbServerController(Subprocess subprocess, String executable) {
        this.subprocess = subprocess;
        this.executable = executable;
    }

    private static String findAdbExecutable(Map<String, String> environment) {
        String androidHome = environment.get("ANDROID_HOME");
        if (androidHome == null || androidHome.isEmpty()) {
            return "adb";
        }
        return androidHome + "/platform-tools/adb";
    }

    private void runWithAdb(String command) throws IOException, InterruptedException {
        Process p = subprocess.execute(new String[]{executable, command});
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue != 0) throw new IOException("adb exited with exit code: " + exitValue);
    }

    public void launch() throws IOException, InterruptedException {
        runWithAdb("start-server");
    }

    public void kill() throws IOException, InterruptedException {
        runWithAdb("kill-server");
    }

    public void root() throws IOException, InterruptedException {
        runWithAdb("root");
    }

    public void unroot() throws IOException, InterruptedException {
        runWithAdb("unroot");
    }

    public void remount() throws IOException, InterruptedException {
        runWithAdb("remount");
    }
}
