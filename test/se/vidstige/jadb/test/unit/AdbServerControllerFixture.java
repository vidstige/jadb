package se.vidstige.jadb.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.AdbServerController;
import se.vidstige.jadb.test.fakes.FakeSubprocess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdbServerControllerFixture {
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
        new AdbServerController(subprocess, environment).launch();
    }

    @Test
    public void testStartServerWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "start-server"}, 0);
        new AdbServerController(subprocess, environment).launch();
    }

    @Test(expected = IOException.class)
    public void testStartServerFails() throws Exception {
        subprocess.expect(new String[]{"adb", "start-server"}, -1);
        new AdbServerController(subprocess, environment).launch();
    }

    @Test
    public void testKillServer() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "kill-server"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerController(subprocess, environment).kill();
    }

    @Test
    public void testKillServerWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "kill-server"}, 0);
        new AdbServerController(subprocess, environment).kill();
    }

    @Test(expected = IOException.class)
    public void testKillServerFails() throws Exception {
        subprocess.expect(new String[]{"adb", "kill-server"}, -1);
        new AdbServerController(subprocess, environment).kill();
    }

    @Test
    public void testRootDevice() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "root"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerController(subprocess, environment).root();
    }

    @Test
    public void testRootDeviceWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "root"}, 0);
        new AdbServerController(subprocess, environment).root();
    }

    @Test(expected = IOException.class)
    public void testRootDeviceFails() throws Exception {
        subprocess.expect(new String[]{"adb", "root"}, -1);
        new AdbServerController(subprocess, environment).root();
    }

    @Test
    public void testUnrootDevice() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "unroot"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerController(subprocess, environment).unroot();
    }

    @Test
    public void testUnrootDeviceWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "unroot"}, 0);
        new AdbServerController(subprocess, environment).unroot();
    }

    @Test(expected = IOException.class)
    public void testUnrootDeviceFails() throws Exception {
        subprocess.expect(new String[]{"adb", "unroot"}, -1);
        new AdbServerController(subprocess, environment).unroot();
    }

    @Test
    public void testRemountDevice() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "remount"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerController(subprocess, environment).remount();
    }

    @Test
    public void testRemountDeviceWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "remount"}, 0);
        new AdbServerController(subprocess, environment).remount();
    }

    @Test(expected = IOException.class)
    public void testRemountDeviceFails() throws Exception {
        subprocess.expect(new String[]{"adb", "remount"}, -1);
        new AdbServerController(subprocess, environment).remount();
    }
}
