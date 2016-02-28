package se.vidstige.jadb;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by vidstige on 2014-03-20
 */
public class RemoteFile {
    private final String path;

    public RemoteFile(String path) { this.path = path;}

    public String getName() { throw new NotImplementedException(); }
    public int getSize() { throw new NotImplementedException(); }
    public long getLastModified() { throw new NotImplementedException(); }
    public boolean isDirectory() { throw new NotImplementedException(); }

    public String getPath() { return path;}

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
