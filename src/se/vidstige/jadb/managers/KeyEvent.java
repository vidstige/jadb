package se.vidstige.jadb.managers;

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
        String[] args = new String[longPress ? 3 : 2];
        args[0] = "keyevent";
        if(longPress) {
            args[1] = "--longpress";
            args[2] = key.toString();
        }
        else {
            args[1] = key.toString();
        }
        return args;
    }

    public static KeyEvent press(KeyCode key) {
        return new KeyEvent(key);
    }
}