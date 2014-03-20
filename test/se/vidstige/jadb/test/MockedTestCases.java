package se.vidstige.jadb.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

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

}
