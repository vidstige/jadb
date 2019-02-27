package se.vidstige.jadb.test.unit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MockedTestCases {

    private FakeAdbServer server;
    private JadbConnection connection;

    @Before
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        connection = new JadbConnection("localhost", 15037);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.verifyExpectations();
    }

    @Test
    public void testGetHostVersion() throws Exception {
        Assert.assertEquals("001f", connection.getHostVersion());
    }

    @Test
    public void testListDevices() throws Exception {
        server.add("serial-123");
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals("serial-123", devices.get(0).getSerial());
    }

    @Test
    public void testGetDeviceState() throws Exception {
        server.add("serial-1", "offline");
        server.add("serial-2", "device");
        server.add("serial-3", "unknown");
        server.add("serial-4", "foobar");
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals(JadbDevice.State.Offline, devices.get(0).getState());
        Assert.assertEquals(JadbDevice.State.Device, devices.get(1).getState());
        Assert.assertEquals(JadbDevice.State.Unknown, devices.get(2).getState());
        Assert.assertEquals(JadbDevice.State.Unknown, devices.get(3).getState());
    }

    @Test
    public void testListNoDevices() throws Exception {
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals(0, devices.size());
    }

    @Test
    public void testPushFile() throws Exception {
        server.add("serial-123");
        server.expectPush("serial-123", new RemoteFile("/remote/path/abc.txt")).withContent("abc");
        JadbDevice device = connection.getDevices().get(0);
        ByteArrayInputStream fileContents = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        device.push(fileContents, parseDate("1981-08-25 13:37"), 0666, new RemoteFile("/remote/path/abc.txt"));
    }

    @Test(expected = JadbException.class)
    public void testPushToInvalidPath() throws Exception {
        server.add("serial-123");
        server.expectPush("serial-123", new RemoteFile("/remote/path/abc.txt")).failWith("No such directory");
        JadbDevice device = connection.getDevices().get(0);
        ByteArrayInputStream fileContents = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        device.push(fileContents, parseDate("1981-08-25 13:37"), 0666, new RemoteFile("/remote/path/abc.txt"));
    }

    @Test
    public void testPullFile() throws Exception {
        server.add("serial-123");
        server.expectPull("serial-123", new RemoteFile("/remote/path/abc.txt")).withContent("foobar");
        JadbDevice device = connection.getDevices().get(0);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        device.pull(new RemoteFile("/remote/path/abc.txt"), buffer);
        Assert.assertArrayEquals("foobar".getBytes(StandardCharsets.UTF_8), buffer.toByteArray());
    }

    @Test
    public void testExecuteShell() throws Exception {
        server.add("serial-123");
        server.expectShell("serial-123", "ls -l").returns("total 0");
        JadbDevice device = connection.getDevices().get(0);
        device.executeShell("ls", "-l");
    }

    @Test
    public void testExecuteEnableTcpip() throws IOException, JadbException {
        server.add("serial-123");
        server.expectTcpip("serial-123", 5555);
        JadbDevice device = connection.getDevices().get(0);
        device.enableAdbOverTCP();
    }

    @Test
    public void testExecuteShellQuotesWhitespace() throws Exception {
        server.add("serial-123");
        server.expectShell("serial-123", "ls 'space file'").returns("space file");
        server.expectShell("serial-123", "echo 'tab\tstring'").returns("tab\tstring");
        server.expectShell("serial-123", "echo 'newline1\nstring'").returns("newline1\nstring");
        server.expectShell("serial-123", "echo 'newline2\r\nstring'").returns("newline2\r\nstring");
        JadbDevice device = connection.getDevices().get(0);
        device.executeShell("ls", "space file");
        device.executeShell("echo", "tab\tstring");
        device.executeShell("echo", "newline1\nstring");
        device.executeShell("echo", "newline2\r\nstring");
    }

    @Test
    public void testFileList() throws Exception {
        server.add("serial-123");
        server.expectList("serial-123", "/sdcard/Documents")
                .withDir("school", 123456789)
                .withDir("finances", 7070707)
                .withDir("\u904A\u6232", 528491)
                .withFile("user_manual.pdf", 3000, 648649)
                .withFile("effective java vol. 7.epub", 0xCAFE, 0xBABE)
                .withFile("\uB9AC\uADF8 \uC624\uBE0C \uB808\uC804\uB4DC", 240, 9001);
        JadbDevice device = connection.getDevices().get(0);
        List<RemoteFile> files = device.list("/sdcard/Documents");
        Assert.assertEquals(6, files.size());
        assertHasDir("school", 123456789, files);
        assertHasDir("finances", 7070707, files);
        assertHasDir("\u904A\u6232", 528491, files);
        assertHasFile("user_manual.pdf", 3000, 648649, files);
        assertHasFile("effective java vol. 7.epub", 0xCAFE, 0xBABE, files);
        assertHasFile("\uB9AC\uADF8 \uC624\uBE0C \uB808\uC804\uB4DC", 240, 9001, files);
    }

    private static long parseDate(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.parse(date).getTime();
    }

    private static void assertHasFile(String expPath, int expSize, long expModifyTime, List<RemoteFile> actualFiles) {
        for (RemoteFile file : actualFiles) {
            if (expPath.equals(file.getPath())) {
                if (file.isDirectory()) {
                    Assert.fail("File " + expPath + " was listed as a dir!");
                } else if (expSize != file.getSize() || expModifyTime != file.getLastModified()) {
                    Assert.fail("File " + expPath + " exists but has incorrect properties!");
                } else {
                    return;
                }
            }
        }
        Assert.fail("File " + expPath + " could not be found!");
    }

    private static void assertHasDir(String expPath, long expModifyTime, List<RemoteFile> actualFiles) {
        for (RemoteFile file : actualFiles) {
            if (expPath.equals(file.getPath())) {
                if (!file.isDirectory()) {
                    Assert.fail("Dir " + expPath + " was listed as a file!");
                } else if (expModifyTime != file.getLastModified()) {
                    Assert.fail("Dir " + expPath + " exists but has incorrect properties!");
                } else {
                    return;
                }
            }
        }
        Assert.fail("Dir " + expPath + " could not be found!");
    }
}
