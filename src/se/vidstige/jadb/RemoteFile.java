package se.vidstige.jadb;

/**
 * Created by vidstige on 2014-03-19.
 */
public class RemoteFile {
    public static final RemoteFile DONE = new RemoteFile("DONE", null, 0, 0, 0);

    private final String name;
    private final int mode;
    private final int size;
    private final long lastModified;

    public RemoteFile(String id, String name, int mode, int size, long lastModified) {
        this.name = name;
        this.mode = mode;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getName() {

        return name;
    }

    public int getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public boolean isDirectory() {
        return (mode & (1 << 14)) == (1 << 14);
    }
}
