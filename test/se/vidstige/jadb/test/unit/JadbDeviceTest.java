package se.vidstige.jadb.test.unit;

import org.junit.Test;
import se.vidstige.jadb.FakeJadbDevice;
import se.vidstige.jadb.ITransportFactory;
import se.vidstige.jadb.JadbDevice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JadbDeviceTest {
    @Test
    public void getprop() throws Exception {
        final String key1 = "bluetooth.hciattach";
        final String value1 = "true";

        final String key2 = "bluetooth.status";
        final String value2 = "off";

        String response = "[" + key1 + "]: [" + value1 + "]\n" +
                "[" + key2 + "]: [" + value2 + "]\n";

        List<InputStream> executeShellResponse = Collections.singletonList((InputStream) new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
        Map<String, String> props = new FakeJadbDevice("serial", "type", null, executeShellResponse).getprop();

        assertNotNull(props.get(key1));
        assertEquals(props.get(key1), value1);

        assertNotNull(props.get(key2));
        assertEquals(props.get(key2), value2);
    }
}