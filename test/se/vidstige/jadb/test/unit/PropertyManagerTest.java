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
    private static final String DEVICE_SERIAL = "serial-123";

    private FakeAdbServer server;
    private JadbDevice device;

    @Before
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        server.add(DEVICE_SERIAL);
        device = new JadbConnection("localhost", 15037).getDevices().get(0);
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
    public void testGetPropsValueHasSpecialCharacters() throws Exception {
        /* Some example properties from Nexus 9:
        [ro.product.model]: [Nexus 9]
        [ro.product.cpu.abilist]: [arm64-v8a,armeabi-v7a,armeabi]
        [ro.retaildemo.video_path]: [/data/preloads/demo/retail_demo.mp4]
        [ro.url.legal]: [http://www.google.com/intl/%s/mobile/android/basic/phone-legal.html]
        [ro.vendor.build.date]: [Tue Nov 1 18:21:23 UTC 2016]
        */
        //Arrange
        Map<String, String> expected = new HashMap<>();
        expected.put("ro.product.model", "Nexus 9");
        expected.put("ro.product.cpu.abilist", "arm64-v8a,armeabi-v7a,armeabi");
        expected.put("ro.retaildemo.video_path", "/data/preloads/demo/retail_demo.mp4");
        expected.put("ro.url.legal", "http://www.google.com/intl/%s/mobile/android/basic/phone-legal.html");
        expected.put("ro.vendor.build.date", "Tue Nov 1 18:21:23 UTC 2016");

        String response = "[ro.product.model]: [Nexus 9]\n" +
                "[ro.product.cpu.abilist]: [arm64-v8a,armeabi-v7a,armeabi]\n" +
                "[ro.retaildemo.video_path]: [/data/preloads/demo/retail_demo.mp4]\n" +
                "[ro.url.legal]: [http://www.google.com/intl/%s/mobile/android/basic/phone-legal.html]\n" +
                "[ro.vendor.build.date]: [Tue Nov 1 18:21:23 UTC 2016]";

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
