package se.vidstige.jadb;

public class AndroidDevice {
	private final String serial;
	
	public AndroidDevice(String serial, String type) {
		this.serial = serial;
	}	
	
	public AndroidDevice(String serial) {
		this(serial, null);
	}
	
	public String getSerial()
	{
		return serial;		
	}
	
	@Override
	public String toString()
	{
		return "Android Device with serial " + serial;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serial == null) ? 0 : serial.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AndroidDevice other = (AndroidDevice) obj;
		if (serial == null) {
			if (other.serial != null)
				return false;
		} else if (!serial.equals(other.serial))
			return false;
		return true;
	}
	
}
