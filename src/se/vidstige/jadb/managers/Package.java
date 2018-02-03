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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package that = (Package) o;

        if (!name.equals(that.name)) return false;

        return true;
        }

    @Override
    public int hashCode() { return name.hashCode(); }
}
