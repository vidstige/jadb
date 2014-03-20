package se.vidstige.jadb.test.fakes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// >set ANDROID_ADB_SERVER_PORT=15037
public class AdbServer implements Runnable {

    private static final int DEFAULT_PORT = 15037;
	private final int port;
	private ServerSocket socket;
	private Thread thread;
	private final Object lockObject = new Object();
	
	public static void main(String[] args)
	{
		AdbServer server = new AdbServer();
		server.run();
	}

    public AdbServer()
    {
        this(DEFAULT_PORT);
    }
    public AdbServer(int port)
    {
        this.port = port;
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
		}
	}

    public void stop() throws IOException, InterruptedException {
        socket.close();
        thread.join();
    }
}
