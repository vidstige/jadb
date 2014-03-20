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
}
