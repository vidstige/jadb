package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbException;

import java.io.IOException;

/**
 * Author: pablobaxter.
 */
public abstract class InputEvent {

    public abstract void execute() throws IOException, JadbException;
}
