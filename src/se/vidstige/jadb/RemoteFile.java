package se.vidstige.jadb;

/**
 * Created by vidstige on 2014-03-19.
 */
class RemoteFile {
    public static final RemoteFile DONE = new RemoteFile("DONE", null);
    private String name;

    public RemoteFile(String id, String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }
}
