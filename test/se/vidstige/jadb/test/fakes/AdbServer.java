package se.vidstige.jadb.test.fakes;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class AdbServer implements Runnable {

	private int port = 15037;
	private ServerSocket socket;
	private Thread thread;
	private Object lockObject = new Object();
	
	public static void main(String[] args)
	{
		AdbServer server = new AdbServer();
		server.run();
	}
		
	public void start() throws InterruptedException
	{
		thread = new Thread(this, "Fake Adb Server");
		thread.setDaemon(true);
		thread.start();
		synchronized (lockObject) {
			lockObject.wait();
		}
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public void run() {
		try {
			System.out.println("Starting on port " + port);
			socket = new ServerSocket(port);
			socket.setReuseAddress(true);
			
			synchronized (lockObject) {
				lockObject.notify();
			}
			
			while (true)
			{				
				Socket c = socket.accept();
				Thread clientThread = new Thread(new AdbResponder(c), "AdbClientWorker");
				clientThread.setDaemon(true);
				clientThread.start();		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
