package se.vidstige.jadb.test.unit;

import org.junit.Assert;
import org.junit.Test;
import se.vidstige.jadb.AdbFilterInputStream;
import se.vidstige.jadb.Stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdbInputStreamFixture {

    private byte[] passthrough(byte[] input) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
        InputStream sut = new AdbFilterInputStream(inputStream);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stream.copy(sut, output);
        return output.toByteArray();
    }

    @Test
    public void testSimple() throws Exception {
        byte[] actual = passthrough(new byte[]{ 1, 2, 3});
        Assert.assertArrayEquals(new byte[]{ 1, 2, 3}, actual);
    }

    @Test
    public void testEmpty() throws Exception {
        byte[] actual = passthrough(new byte[]{});
        Assert.assertArrayEquals(new byte[]{}, actual);
    }

    @Test
    public void testSimpleRemoval() throws Exception {
        byte[] actual = passthrough(new byte[]{0x0d, 0x0a});
        Assert.assertArrayEquals(new byte[]{0x0a}, actual);
    }

    @Test
    public void testDoubleRemoval() throws Exception {
        byte[] actual = passthrough(new byte[]{0x0d, 0x0a, 0x0d, 0x0a});
        Assert.assertArrayEquals(new byte[]{0x0a, 0x0a}, actual);
    }
}
