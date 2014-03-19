package se.vidstige.jadb;

import java.io.File;

/**
 * Created by vidstige on 2014-03-19.
 */
class DirectoryEntry {
    public static final DirectoryEntry DONE = new DirectoryEntry("DONE", null);
    private String name;

    public DirectoryEntry(String id, String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }
}
