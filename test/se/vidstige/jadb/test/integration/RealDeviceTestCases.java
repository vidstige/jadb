package se.vidstige.jadb.test.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import se.vidstige.jadb.AdbServerLauncher;
import se.vidstige.jadb.ConnectionToRemoteDeviceException;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.Stream;
import se.vidstige.jadb.Subprocess;

import static org.junit.Assert.*;


public class RealDeviceTestCases {

    private JadbConnection jadb;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder(); //Must be public

    @BeforeClass
    public static void tryToStartAdbServer() {
        try {
            new AdbServerLauncher(new Subprocess(), System.getenv()).launch();
        } catch (IOException | InterruptedException e) {
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
    public void testShellProcessBuilderStart() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        Process process = any.shellProcessBuilder("ls /").start();
        AtomicReference<String> stdout = new AtomicReference<>();
        AtomicReference<String> stderr = new AtomicReference<>();
        Thread thread1 = gobbler(process.getInputStream(), stdout);
        Thread thread2 = gobbler(process.getErrorStream(), stderr);
        thread1.start();
        thread2.start();
        process.waitFor();
        thread1.join();
        thread2.join();
        System.out.println(stdout.get());
        System.out.println(stderr.get());
    }

    private Thread gobbler(final InputStream stream, final AtomicReference<String> out) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                out.set(new Scanner(stream).useDelimiter("\\A").next());
            }
        });
    }

    @Test
    public void testShellExecuteProcessRedirectToOutputStream() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Process process = any.shellProcessBuilder("ls /")
            .redirectOutput(out)
            .redirectError(err)
            .start();
        process.waitFor();
        System.out.println(out.toString(StandardCharsets.UTF_8.name()));
        System.out.println(err.toString(StandardCharsets.UTF_8.name()));
    }

    @Test
    public void testShellExecuteProcessRedirectErrorStream() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        Process process = any.shellProcessBuilder("ls /").redirectErrorStream(true).start();
        String stdout = new Scanner(process.getInputStream()).useDelimiter("\\A").next();
        process.waitFor();
        System.out.println(stdout);
    }

    @Test
    public void testScreenshot() throws Exception {
        JadbDevice any = jadb.getAnyDevice();
        try (FileOutputStream outputStream = new FileOutputStream(temporaryFolder.newFile("screenshot.png"))) {
            InputStream stdout = any.executeShell("screencap", "-p");
            Stream.copy(stdout, outputStream);
        }
    }

    /**
     * This test requires emulator running on non-standard tcp port - this may be achieve by executing such command:
     * ${ANDROID_HOME}/emulator -verbose -avd ${NAME} -ports 10000,10001
     *
     * @throws IOException
     * @throws JadbException
     * @throws ConnectionToRemoteDeviceException
     */
    @Test
    public void testConnectionToTcpDevice() throws IOException, JadbException, ConnectionToRemoteDeviceException {
        jadb.connectToTcpDevice(new InetSocketAddress("127.0.0.1", 10001));
        List<JadbDevice> devices = jadb.getDevices();

        assertNotNull(devices);
        assertFalse(devices.isEmpty());
    }

    /**
     * @see #testConnectionToTcpDevice()
     *
     * @throws IOException
     * @throws JadbException
     * @throws ConnectionToRemoteDeviceException
     */
    @Test
    public void testDisconnectionToTcpDevice() throws IOException, JadbException, ConnectionToRemoteDeviceException {
        testConnectionToTcpDevice();

        jadb.disconnectFromTcpDevice(new InetSocketAddress("127.0.0.1", 10001));
        jadb.getDevices();

        List<JadbDevice> devices = jadb.getDevices();
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }
}
