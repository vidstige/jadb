package se.vidstige.jadb.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.managers.PropertyManager;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PropertyManagerTest {
    private final String DEVICE_SERIAL = "serial-123";

    private FakeAdbServer server;
    private JadbConnection connection;
    private JadbDevice device;

    @Before
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        server.add(DEVICE_SERIAL);
        connection = new JadbConnection("localhost", 15037);

        device = connection.getDevices().get(0);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.verifyExpectations();
    }

    @Test
    public void testGetPropsStandardFormat() throws Exception {
        //Arrange
        Map<String, String> expected = new HashMap<>();
        expected.put("bluetooth.hciattach", "true");
        expected.put("bluetooth.status", "off");

        String response = "[bluetooth.status]: [off] \n" +
                "[bluetooth.hciattach]: [true]";

        server.expectShell(DEVICE_SERIAL, "getprop").returns(response);

        //Act
        Map<String, String> actual = new PropertyManager(device).getprop();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPropsMalformedIgnoredString() throws Exception {
        //Arrange
        Map<String, String> expected = new HashMap<>();
        expected.put("bluetooth.hciattach", "true");
        expected.put("bluetooth.status", "off");

        String response = "[bluetooth.status]: [off]\n" +
                "[malformed_line]\n" +
                "[bluetooth.hciattach]: [true]";

        server.expectShell(DEVICE_SERIAL, "getprop").returns(response);

        //Act
        Map<String, String> actual = new PropertyManager(device).getprop();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPropsMalformedNotUsedString() throws Exception {
        //Arrange
        Map<String, String> expected = new HashMap<>();
        expected.put("bluetooth.status", "off");

        String response = "[bluetooth.status]: [off]\n" +
                "malformed[bluetooth.hciattach]: [true]";

        server.expectShell(DEVICE_SERIAL, "getprop").returns(response);

        //Act
        Map<String, String> actual = new PropertyManager(device).getprop();

        //Assert
        assertEquals(expected, actual);
    }
}
