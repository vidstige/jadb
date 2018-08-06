package se.vidstige.jadb.test.fakes;

import se.vidstige.jadb.Subprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeSubprocess extends Subprocess {
    private List<Expectation> expectations = new ArrayList<>();

    public Expectation expect(String[] command, int exitValue) {
        Expectation builder = new Expectation(command, exitValue);
        expectations.add(builder);
        return builder;
    }

    private String format(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(command[i]);
        }
        return sb.toString();
    }

    @Override
    public Process execute(String[] command) throws IOException {
        List<Expectation> toRemove = new ArrayList<>();
        for (Expectation e : expectations) {
            if (e.matches(command)) {
                toRemove.add(e);
            }
        }
        expectations.removeAll(toRemove);
        if (toRemove.size() == 1) {
            return new FakeProcess(toRemove.get(0).getExitValue());
        }
        throw new AssertionError("Unexpected command: " + format(command));
    }

    public void verifyExpectations() {
        if (expectations.size() > 0) {
            throw new AssertionError("Subprocess never called: " + format(expectations.get(0).getCommand()));
        }
    }

    private static class Expectation {
        private final String[] command;
        private final int exitValue;

        public Expectation(String[] command, int exitValue) {
            this.command = command;
            this.exitValue = exitValue;
        }

        public boolean matches(String[] command) {
            return Arrays.equals(command, this.command);
        }

        public int getExitValue() {
            return exitValue;
        }

        public String[] getCommand() {
            return command;
        }
    }
}
