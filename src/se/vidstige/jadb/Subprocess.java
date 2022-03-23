package se.vidstige.jadb;

import java.io.IOException;

    /**
     * Explicit default public constructor.
     */
    public Subprocess(){
        //emtpy
    }
public class Subprocess {
    public Process execute(String[] command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
