#JADB#
ADB client implemented in pure Java.

The Android Debug Bridge or ADB for short it a client-server architecture used to install android apps from an IDE or command line and to debug apps, etc.

The Android SDK Tools is available for the major platforms (Mac, Windows & Linux) and in there is a command line tool called adb that implements the ADB protocol.

This projects aims at providing an up to date implementation of the ADB protocol.

[![Build Status](https://travis-ci.org/vidstige/jadb.svg?branch=master)](https://travis-ci.org/vidstige/jadb)

## Example ##
Usage cannot be simpler. Just create a `JadbConnection` and off you go.

    JadbConnection jadb = new JadbConnection();
    List<JadbDevice> devices = jadb.getDevices();

Make sure the adb server is running. You can start it by running `adb` once from the command line.

It's very easy to send and receive files from your android device, for example as below.

    JadbDevice device = ...
    device.pull(new RemoteFile("/path/to/file.txt"), new File("file.txt"));

Some high level operations such as installing and uninstalling packages are also available.

    JadbDevice device = ...
    new PackageManager(device).install(new File("/path/to/my.apk"));

## Protocol Description ##

An overview of the protocol can be found here: [Overview](https://github.com/cgjones/android-system-core/blob/master/adb/OVERVIEW.TXT)

A list of the available commands that a ADB Server may accept can be found here:
[Services](https://github.com/cgjones/android-system-core/blob/master/adb/SERVICES.TXT)


## Using JADB in your application ##

Since version v1.1 Jadb support [maven](https://maven.apache.org/) as a build system. Although this project is not presented in official apache maven 
repositories this library can be used as dependencies in your maven/gradle project with the help of [jitpack](https://jitpack.io). 
[Jitpack](https://jitpack.io) is a system which parses github public repositories and make artifacts from them. 
You only need to add [jitpack](https://jitpack.io) as a repository to let maven/gradle to search for artifacts in it, like so

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
 
After that you will need to add actual dependency. [Jitpack](https://jitpack.io) takes groupId, artifactId and version id from repository name, 
project name and tag ignoring actual values from pom.xml. So you need to write:
 
```
<dependency>
    <groupId>com.github.vidstige</groupId>
    <artifactId>jadb</artifactId>
    <version>v1.1</version>
</dependency>
```

## Contributing ##
This project would not be where it is, if it where not for the helpful [contributors](https://github.com/vidstige/jadb/graphs/contributors)
supporting jadb with pull requests, issue reports, and great ideas. If _you_ would like to
contribute, please read through [CONTRIBUTING.md](CONTRIBUTING.md).

## Authors ##
Samuel Carlsson <samuel.carlsson@gmai.com>

See [contributors](https://github.com/vidstige/jadb/graphs/contributors) for a full list.

## License ##
This project is released under the Apache License Version 2.0, see [LICENSE.md](LICENSE.md) for more information.
