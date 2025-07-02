package se.vidstige.jadb;

public class RemoteDir extends RemoteFile {
    public RemoteDir(RemoteFile remoteFile) {
        this(remoteFile.getPath());
    }

    public RemoteDir(String path) {
        super(path);
    }
}
