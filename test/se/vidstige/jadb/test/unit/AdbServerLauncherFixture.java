package se.vidstige.jadb.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.AdbServerLauncher;
import se.vidstige.jadb.test.fakes.FakeSubprocess;

import java.io.IOException;
import java.util.*;

public class AdbServerLauncherFixture {

    private FakeSubprocess subprocess;
    private Map<String, String> environment = new HashMap<>();

    @Before
    public void setUp() {
        subprocess = new FakeSubprocess();
    }
    @After
    public void tearDown() {
        subprocess.verifyExpectations();
    }

    @Test
    public void testStartServer() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "start-server"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerLauncher(subprocess, environment).launch();
    }

    @Test
    public void testStartServerWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "start-server"}, 0);
        new AdbServerLauncher(subprocess, environment).launch();
    }

    @Test(expected=IOException.class)
    public void testStartServerFails() throws Exception {
        subprocess.expect(new String[]{"adb", "start-server"}, -1);
        new AdbServerLauncher(subprocess, environment).launch();
    }
}
