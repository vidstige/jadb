package se.vidstige.jadb.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.managers.Package;
import se.vidstige.jadb.managers.PackageManager;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PackageManagerTest {
    private final String DEVICE_SERIAL = "serial-123";

    private FakeAdbServer server;
    private JadbConnection connection;
    private JadbDevice device;

    @Before
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        server.add(DEVICE_SERIAL);
        connection = new JadbConnection("localhost", 15037);

        device = connection.getDevices().get(0);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.verifyExpectations();
    }

    @Test
    public void testGetPackagesWithSeveralPackages() throws Exception {
        //Arrange
        List<Package> expected = new ArrayList<>();
        expected.add(new Package("/system/priv-app/Contacts.apk-com.android.contacts"));
        expected.add(new Package("/system/priv-app/Teleservice.apk-com.android.phone"));

        String response = "package:/system/priv-app/Contacts.apk-com.android.contacts\n" +
                "package:/system/priv-app/Teleservice.apk-com.android.phone";

        server.expectShell(DEVICE_SERIAL, "pm list packages").returns(response);

        //Act
        List<Package> actual = new PackageManager(device).getPackages();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackagesMalformedIgnoredString() throws Exception {
        //Arrange
        List<Package> expected = new ArrayList<>();
        expected.add(new Package("/system/priv-app/Contacts.apk-com.android.contacts"));
        expected.add(new Package("/system/priv-app/Teleservice.apk-com.android.phone"));

        String response = "package:/system/priv-app/Contacts.apk-com.android.contacts\n" +
                "[malformed_line]\n" +
                "package:/system/priv-app/Teleservice.apk-com.android.phone";

        server.expectShell(DEVICE_SERIAL, "pm list packages").returns(response);

        //Act
        List<Package> actual = new PackageManager(device).getPackages();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackagesWithNoPackages() throws Exception {
        //Arrange
        List<Package> expected = new ArrayList<>();
        String response = "";

        server.expectShell(DEVICE_SERIAL, "pm list packages").returns(response);

        //Act
        List<Package> actual = new PackageManager(device).getPackages();

        //Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testWithForwardLock() throws Exception {
        PackageManager.InstallOption withForwardLock = PackageManager.WITH_FORWARD_LOCK;
        // Letter L not number 1
        String expected = "-l";
        Method privateMethod = withForwardLock.getClass().getDeclaredMethod("getStringRepresentation");
        privateMethod.setAccessible(true);
        String actual = (String) privateMethod.invoke(withForwardLock);

        assertEquals(expected, actual);
    }

    @Test
    public void testWithInstallerPackageName() throws Exception {
        PackageManager.InstallOption withInstallerPackageName = PackageManager.WITH_INSTALLER_PACKAGE_NAME("aaa bbb");
        String expected = "-t aaa bbb";
        Method privateMethod = withInstallerPackageName.getClass().getDeclaredMethod("getStringRepresentation");
        privateMethod.setAccessible(true);
        String actual = (String) privateMethod.invoke(withInstallerPackageName);

        assertEquals(expected, actual);
    }
}