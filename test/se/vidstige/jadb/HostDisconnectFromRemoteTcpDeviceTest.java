package se.vidstige.jadb;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HostDisconnectFromRemoteTcpDeviceTest {

    @Test
    public void testNormalConnection() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("disconnected host:1");

        InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

        //Do
        HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport);
        InetSocketAddress resultInetSocketAddress = hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);

        //Validate
        assertEquals(inetSocketAddress, resultInetSocketAddress);
    }

    @Test(expected = JadbException.class)
    public void testTransportLevelException() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        doThrow(new JadbException("Fake exception")).when(transport).verifyResponse();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

        //Do
        HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport);
        hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);
    }

    @Test(expected = ConnectionToRemoteDeviceException.class)
    public void testProtocolException() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("any string");
        HostDisconnectFromRemoteTcpDevice.ResponseValidator responseValidator = mock(HostConnectionCommand.ResponseValidator.class);
        doThrow(new ConnectionToRemoteDeviceException("Fake exception")).when(responseValidator).validate(anyString());

        InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

        //Do
        HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport, responseValidator);
        hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);
    }

    @Test
    public void testProtocolResponseValidatorSuccessfullyConnected() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("disconnected 127.0.0.1:10001");
    }

    @Test
    public void testProtocolResponseValidatorAlreadyConnected() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("error: no such device '127.0.0.1:10001'");
    }

    @Test(expected = ConnectionToRemoteDeviceException.class)
    public void testProtocolResponseValidatorErrorInValidate() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("some error occurred");
    }
}
