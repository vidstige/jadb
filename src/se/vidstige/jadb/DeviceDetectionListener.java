package se.vidstige.jadb;

import java.util.List;

public interface DeviceDetectionListener {
    public boolean detect(List<JadbDevice> device);
}

