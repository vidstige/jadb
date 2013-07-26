package se.vidstige.jadb.test;

import java.util.List;

import org.junit.Test;

import se.vidstige.jadb.AndroidDevice;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbException;

public class AndroidDeviceTestCases {

	@Test
	public void testGetState() throws Exception {
		JadbConnection jadb = new JadbConnection();
		List<AndroidDevice> devices = jadb.getDevices();
		AndroidDevice device = devices.get(0);
		device.getState();		
	}
	
	@Test(expected=JadbException.class)
	public void invalidShellCommand() throws Exception
	{
		JadbConnection jadb = new JadbConnection("localhost", 5037);
		List<AndroidDevice> devices = jadb.getDevices();
		AndroidDevice device = devices.get(0);
		device.executeShell("cmd", "foo", "bar");
	}
}
