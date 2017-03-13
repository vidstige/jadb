package se.vidstige.jadb.test.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import se.vidstige.jadb.*;
import se.vidstige.jadb.managers.BatteryManager;
import se.vidstige.jadb.managers.InputManager;
import se.vidstige.jadb.managers.KeyCode;
import se.vidstige.jadb.managers.KeyEvent;

import java.io.*;

import java.util.List;

public class RealDeviceTestCases {

    private JadbConnection jadb;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder(); //Must be public

    @BeforeClass
    public static void tryToStartAdbServer() {
        try {
            new AdbServerLauncher(new Subprocess(), System.getenv()).launch();
        } catch (IOException e) {
            System.out.println("Could not start adb-server");
        } catch (InterruptedException e) {
            System.out.println("Could not start adb-server");
        }
    }

    @Before
    public void connect() throws IOException {
        try {
            jadb = new JadbConnection();
            jadb.getHostVersion();
        } catch (Exception e) {
            org.junit.Assume.assumeNoException(e);
        }
    }

    @Test
    public void testGetHostVersion() throws Exception {
        jadb.getHostVersion();
    }

    @Test
    public void testGetDevices() throws Exception {
        List<JadbDevice> actual = jadb.getDevices();
        Assert.assertNotNull(actual);
        //Assert.assertEquals("emulator-5554", actual.get(0).getSerial());
    }

    @Test
    public void testListFilesTwice() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        for (RemoteFile f : any.list("/")) {
            System.out.println(f.getPath());
        }

        for (RemoteFile f : any.list("/")) {
            System.out.println(f.getPath());
        }
    }

    @Test
    public void testPushFile() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        any.push(new File("README.md"), new RemoteFile("/sdcard/README.md"));
        //second read on the same device
        any.push(new File("README.md"), new RemoteFile("/sdcard/README.md"));
    }

    @Test(expected = JadbException.class)
    public void testPushFileToInvalidPath() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        any.push(new File("README.md"), new RemoteFile("/no/such/directory/README.md"));
    }

    @Test
    public void testPullFile() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        any.pull(new RemoteFile("/sdcard/README.md"), temporaryFolder.newFile("foobar.md"));
        //second read on the same device
        any.pull(new RemoteFile("/sdcard/README.md"), temporaryFolder.newFile("foobar.md"));
    }

    @Test(expected = JadbException.class)
    public void testPullInvalidFile() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        any.pull(new RemoteFile("/file/does/not/exist"), temporaryFolder.newFile("xyz"));
    }

    @SuppressWarnings("deprecation")
	@Test
    public void testShellExecuteTwice() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        any.executeShell(bout, "ls /");
        any.executeShell(bout, "ls", "-la", "/");
        System.out.write(bout.toByteArray());
    }

    @Test
    public void testScreenshot() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(temporaryFolder.newFile("screenshot.png"));
            InputStream stdout = any.executeShell("screencap", "-p");
            Stream.copy(stdout, outputStream);
        }  finally {
            if (outputStream != null) outputStream.close();
        }
    }

    @Test
    public void testInputScreenOnOff() throws Exception{
        JadbDevice any = jadb.getAnyDevice();
        InputManager inputManager = new InputManager(any);
        inputManager.sendEvent(KeyEvent.press(KeyCode.KEYCODE_POWER));
    }

    @Test
    public void testUsbDisconnect() throws Exception{
        JadbDevice any = jadb.getAnyDevice();
        BatteryManager batteryManager = new BatteryManager(any);
        batteryManager.simulateUsbUnplug();
    }

    @Test
    public void testUsbReset() throws Exception{
        JadbDevice any = jadb.getAnyDevice();
        BatteryManager batteryManager = new BatteryManager(any);
        batteryManager.resetUsbPlug();
    }
}
