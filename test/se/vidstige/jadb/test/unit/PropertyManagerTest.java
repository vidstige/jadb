package se.vidstige.jadb.test.unit;

import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.managers.PropertyManager;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertyManagerTest {
    private FakeAdbServer server;
    private JadbConnection connection;

    @Before
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        connection = new JadbConnection("localhost", 15037);
    }

    @Test
    public void testGetPropsStandardFormat() throws Exception {
        final String key1 = "bluetooth.hciattach";
        final String value1 = "true";

        final String key2 = "bluetooth.status";
        final String value2 = "off";

        String format = "[%s]: [%s] \n";

        String response = String.format(format, key1, value1) + String.format(format, key2, value2);


        server.add("serial-123");
        server.expectShell("serial-123", "getprop").returns(response);
        JadbDevice device = connection.getDevices().get(0);
        Map<String, String> props = new PropertyManager(device).getprop();

        assertNotNull(props.get(key1));
        assertEquals(props.get(key1), value1);

        assertNotNull(props.get(key2));
        assertEquals(props.get(key2), value2);
    }

    @Test
    public void testGetPropsMalformedString() throws Exception {
        final String key1 = "bluetooth.hciattach";
        final String value1 = "true";

        final String key2 = "bluetooth.status";
        final String value2 = "off";

        String format = "[%s]: [%s] \n";

        String response1 = String.format(format, key1, value1) + "[malformed]" + String.format(format, key2, value2);
        String response2 = String.format(format, key1, value1) + "[malformed]\n" + String.format(format, key2, value2);


        server.add("serial-123");
        JadbDevice device = connection.getDevices().get(0);
        server.expectShell("serial-123", "getprop").returns(response1);

        //Test1
        Map<String, String> props1 = new PropertyManager(device).getprop();

        assertNotNull(props1.get(key1));
        assertEquals(props1.get(key1), value1);

        //Test2
        server.expectShell("serial-123", "getprop").returns(response2);
        Map<String, String> props2 = new PropertyManager(device).getprop();

        assertNotNull(props2.get(key1));
        assertEquals(props2.get(key1), value1);

        assertNotNull(props2.get(key2));
        assertEquals(props2.get(key2), value2);
    }
}