package se.vidstige.jadb.managers;

public class Bash {
    public static String quote(String s) {
        // TODO: Should also check other whitespace
        if (!s.contains(" ")) {
            return s;
        }
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
