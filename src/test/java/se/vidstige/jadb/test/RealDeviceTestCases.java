package se.vidstige.jadb.test;

import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.File;
import java.util.List;

public class RealDeviceTestCases {

	@Test
	public void testGetHostVersion() throws Exception {
		JadbConnection jadb = new JadbConnection();
		jadb.getHostVersion();
	}
	
	@Test
	public void testGetDevices() throws Exception
	{
		//JadbConnection jadb = new JadbConnection("localhost", 15037);
		JadbConnection jadb = new JadbConnection();
		List<JadbDevice> actual = jadb.getDevices();
		//Assert.assertEquals("emulator-5554", actual.get(0).getSerial());
	}

    @Test
    public void testListFiles() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        JadbDevice any = jadb.getAnyDevice();
        List<RemoteFile> files = any.list("/");
        for (RemoteFile f : files)
        {
            System.out.println(f.getPath());
        }
    }

    @Test
    public void testPushFile() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        JadbDevice any = jadb.getAnyDevice();
        any.push(new File("README.md"), new RemoteFile("/sdcard/README.md"));
    }

    @Test(expected = JadbException.class)
    public void testPushFileToInvalidPath() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        JadbDevice any = jadb.getAnyDevice();
        any.push(new File("README.md"), new RemoteFile("/no/such/directory/README.md"));
    }

    @Test
    public void testPullFile() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        JadbDevice any = jadb.getAnyDevice();
        any.pull(new RemoteFile("/sdcard/README.md"), new File("foobar.md"));
    }

    @Test(expected = JadbException.class)
    public void testPullInvalidFile() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        JadbDevice any = jadb.getAnyDevice();
        any.pull(new RemoteFile("/file/does/not/exist"), new File("xyz"));
    }
}
