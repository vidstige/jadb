package se.vidstige.jadb.test;

import java.util.List;

import org.junit.*;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.test.fakes.AdbServer;

public class MockedTestCases {

    private AdbServer server;
    private JadbConnection connection;

    @Before
    public void setUp() throws Exception{
        server = new AdbServer();
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
        List<JadbDevice> devices = connection.getDevices();
        Assert.assertEquals("X",  devices.get(0).getSerial());
    }
}
