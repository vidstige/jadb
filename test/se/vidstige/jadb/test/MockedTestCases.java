package se.vidstige.jadb.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.server.AdbServer;
import se.vidstige.jadb.server.SocketServer;

import java.util.List;

public class MockedTestCases {

    private SocketServer server;
    private JadbConnection connection;

    @Before
    public void setUp() throws Exception{
        server = new AdbServer(15037);
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
        Assert.assertEquals("X", devices.get(0).getSerial());
    }
}
