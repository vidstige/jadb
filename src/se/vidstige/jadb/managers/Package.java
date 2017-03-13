package se.vidstige.jadb.managers;

/**
 * Android package
 */
public class Package {
    private final String name;

    public Package(String name) {
        this.name = name;
    }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Package && name.equals(((Package) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}
