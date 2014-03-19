package se.vidstige.jadb.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.vidstige.jadb.AndroidDevice;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.test.fakes.AdbServer;

public class JadbTestCases {

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
		List<AndroidDevice> actual = jadb.getDevices();
		//Assert.assertEquals("emulator-5554", actual.get(0).getSerial());
	}

    @Test
    public void testListFiles() throws Exception
    {
        JadbConnection jadb = new JadbConnection();
        AndroidDevice any = jadb.getAnyDevice();
        List<RemoteFile> files = any.list("/");
        for (RemoteFile f : files)
        {
            System.out.println(f.getName());
        }
    }
}
