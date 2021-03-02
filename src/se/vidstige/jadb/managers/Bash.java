package se.vidstige.jadb.managers;

public class Bash {
    private Bash() {
        throw new IllegalStateException("Utility class");
    }

    public static String quote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
