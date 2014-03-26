package se.vidstige.jadb.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MockedTestCases {

    private FakeAdbServer server;
    private JadbConnection connection;

    @Before
    public void setUp() throws Exception{
        server = new FakeAdbServer(15037);
        server.start();
        connection = new JadbConnection("localhost", 15037);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
        server.stop();
        server.verifyExpectations();
    }

    @Test
    public void testListDevices() throws Exception {
        server.add("serial-123");
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals("serial-123", devices.get(0).getSerial());
    }

    @Test
    public void testListNoDevices() throws Exception {
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals(0, devices.size());
    }

    @Test
    public void testPushFile() throws Exception {
        server.add("serial-123");
        server.expectPush("serial-123", new RemoteFile("/remote/path/abc.txt"), "abc");
        JadbDevice device = connection.getDevices().get(0);
        ByteArrayInputStream fileContents = new ByteArrayInputStream("abc".getBytes());
        device.push(fileContents, parseDate("1981-08-25 13:37"), 0666, new RemoteFile("/remote/path/abc.txt"));
    }


    private long parseDate(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.parse(date).getTime();
    }

}
