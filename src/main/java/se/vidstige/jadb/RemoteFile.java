package se.vidstige.jadb;

/**
 * Created by vidstige on 2014-03-20
 */
public class RemoteFile {
    private final String path;

    public RemoteFile(String path) {
        this.path = path;
    }

    public String getName() {
        throw new IllegalStateException("Not implemented");
    }

    public int getSize() {
        throw new IllegalStateException("Not implemented");
    }

    public long getLastModified() {
        throw new IllegalStateException("Not implemented");
    }

    public boolean isDirectory() {
        throw new IllegalStateException("Not implemented");
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteFile that = (RemoteFile) o;

        if (!path.equals(that.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
