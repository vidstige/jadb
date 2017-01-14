package se.vidstige.jadb.managers;

/**
 * Author: pablobaxter.
 */
public class Component {
    
    private Package mPackage;
    private String mComponent;
    
    public Component(String pkg, String component){
        this(new Package(pkg), component);
    }
    
    public Component(Package pkg, String component){
        mPackage = pkg;
        mComponent = component;
    }
    
    Package getPackage(){
        return mPackage;
    }
    
    String getComponent(){
        return mComponent;
    }

    String generateComponent(){
        return mPackage + "/" + mComponent;
    }
    
    @Override
    public String toString(){
        return mComponent;
    }
}
