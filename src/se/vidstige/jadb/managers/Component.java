package se.vidstige.jadb.managers;

/**
 * Author: pablobaxter.
 */
public class Component {
    
    private Package pkg;
    private String component;
    
    public Component(String pkg, String component) {
        this(new Package(pkg), component);
    }
    
    public Component(Package pkg, String component) {
        this.pkg = pkg;
        this.component = component;
    }
    
    Package getPackage() {
        return pkg;
    }
    
    String getComponent() {
        return component;
    }

    String generateComponent() {
        return pkg + "/" + component;
    }
    
    @Override
    public String toString() {
        return component;
    }
}
