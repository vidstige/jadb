package se.vidstige.jadb;

import java.io.File;

/**
 * Created by vidstige on 2014-03-20
 */
public class RemoteFile {
    private final String path;

    public RemoteFile(String path) {
        this.path = path;
    }

    public String getName() {
        int var1 = path.lastIndexOf(File.separatorChar);
        return var1 < 0 ? path : path.substring(var1 + 1);
    }

    public int getSize() {
        throw new UnsupportedOperationException();
    }

    public int getLastModified() {
        throw new UnsupportedOperationException();
    }

    public boolean isDirectory() {
        throw new UnsupportedOperationException();
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteFile that = (RemoteFile) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
