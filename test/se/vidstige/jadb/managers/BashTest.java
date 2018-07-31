package se.vidstige.jadb.managers;

import org.junit.Test;

import static org.junit.Assert.*;

public class BashTest {

    @Test
    public void quote() {
        // http://wiki.bash-hackers.org/syntax/quoting#strong_quoting
        assertEquals("'-t '\\''aaa'\\'''", Bash.quote("-t 'aaa'"));
    }
}