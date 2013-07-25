package se.vidstige.jadb.test;

import org.junit.Test;

import se.vidstige.jadb.JadbConnection;

public class JadbTestCases {

	@Test
	public void testGetHostVersion() throws Exception {
		JadbConnection jadb = new JadbConnection();
		jadb.getHostVersion();
	}
	
	@Test
	public void testGetDevices() throws Exception
	{
		JadbConnection jadb = new JadbConnection();
		jadb.getDevices();
	}
}
