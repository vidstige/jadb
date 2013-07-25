package se.vidstige.jadb.test;

import java.util.List;

import org.junit.Test;

import se.vidstige.jadb.AndroidDevice;
import se.vidstige.jadb.JadbConnection;

public class AndroidDeviceTestCases {

	@Test
	public void test() throws Exception {
		JadbConnection jadb = new JadbConnection();
		List<AndroidDevice> devices = jadb.getDevices();
		AndroidDevice device = devices.get(0);
		device.getStatus();		
	}
}
