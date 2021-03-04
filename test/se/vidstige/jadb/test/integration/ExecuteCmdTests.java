package se.vidstige.jadb.test.integration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ExecuteCmdTests {
    private static JadbConnection jadb;
    private static JadbDevice jadbDevice;

    @Parameterized.Parameter
    public String input;


    @Parameterized.Parameters(name="Test {index} input={0}")
    public static Collection input() {
        return Arrays.asList(new Object[]{
                "öäasd",
                "asf dsa",
                "sdf&g",
                "sd& fg",
                "da~f",
                "asd'as",
                "a¡f",
                "asüd",
                "adös tz",
                "⾀",
                "å",
                "æ",
                "{}"});
    }

    @BeforeClass
    public static void connect() {
        try {
            jadb = new JadbConnection();
            jadb.getHostVersion();
            jadbDevice = jadb.getAnyDevice();
        } catch (Exception e) {
            org.junit.Assume.assumeNoException(e);
        }
    }


    @Test
    public void testExecuteWithSpecialChars() throws Exception {
        InputStream response = jadbDevice.execute("echo", input);
        Assert.assertEquals(input, Stream.readAll(response, StandardCharsets.UTF_8).replaceAll("\n$", ""));
    }
}
