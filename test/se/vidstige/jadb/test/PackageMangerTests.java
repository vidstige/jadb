package se.vidstige.jadb.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.managers.Package;
import se.vidstige.jadb.managers.PackageManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PackageMangerTests {
    private static JadbConnection jadb;
    private PackageManager pm;

    @BeforeClass
    public static void connect() throws IOException {
        try {
            jadb = new JadbConnection();
            jadb.getHostVersion();
        } catch (Exception e) {
            org.junit.Assume.assumeNoException(e);
        }
    }

    @Before
    public void createPackageManager()
    {
        pm = new PackageManager(jadb.getAnyDevice());
    }

    @Test
    public void testLaunchActivity() throws Exception {
        pm.launch(new Package("com.android.settings"));
    }

    @Test
    public void testListPackages() throws Exception {
        List<Package> packages = pm.getPackages();
        for (Package p : packages) {
            System.out.println(p);
        }
    }

    @Test
    public void testInstallUninstallCycle() throws Exception {
        File f = new File("test/data/Tiniest_Smallest_APK_ever_v_platformBuildVersionName=_apkpure.com.apk");
        pm.install(f);
        pm.uninstall(new Package("b.a"));
    }
}
