package se.vidstige.jadb.managers;

import java.util.ArrayList;

/**
 * Author: pablobaxter.
 */
public class KeyEvent extends InputEvent {

    private boolean longPress;
    private KeyCode key;

    private KeyEvent(KeyCode key) {
        this.key = key;
    }

    KeyEvent longPress() {
        longPress = true;
        return this;
    }

    @Override
    protected String[] buildArgs() {
        ArrayList<String> args = new ArrayList<>();
        args.add("keyevent");
        if (longPress) {
           args.add("--longpress");
        }
        args.add(key.toString());
        return args.toArray(new String[args.size()]);
    }

    public static KeyEvent press(KeyCode key) {
        return new KeyEvent(key);
    }
}
