package se.vidstige.jadb;

import org.junit.Test;
import se.vidstige.jadb.entities.TcpAddressEntity;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HostConnectToRemoteTcpDeviceTest {

    @Test
    public void testNormalConnection() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("connected to host:1");

        TcpAddressEntity tcpAddressEntity = new TcpAddressEntity("host", 1);

        //Do
        HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport);
        TcpAddressEntity resultTcpAddressEntity = hostConnectToRemoteTcpDevice.connect(tcpAddressEntity);

        //Validate
        assertEquals(resultTcpAddressEntity, tcpAddressEntity);
    }

    @Test(expected = JadbException.class)
    public void testTransportLevelException() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        TcpAddressEntity tcpAddressEntity = new TcpAddressEntity("host", 1);
        doThrow(new JadbException("Fake exception")).when(transport).verifyResponse();

        //Do
        HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport);
        hostConnectToRemoteTcpDevice.connect(tcpAddressEntity);
    }

    @Test(expected = ConnectionToRemoteDeviceException.class)
    public void testProtocolException() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        //Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("connected to host:1");
        HostConnectToRemoteTcpDevice.ResponseValidator responseValidator = mock(HostConnectToRemoteTcpDevice.ResponseValidator.class);
        doThrow(new ConnectionToRemoteDeviceException("Fake exception")).when(responseValidator).validate(anyString());

        TcpAddressEntity tcpAddressEntity = new TcpAddressEntity("host", 1);

        //Do
        HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport, responseValidator);
        hostConnectToRemoteTcpDevice.connect(tcpAddressEntity);
    }

    @Test
    public void testProtocolResponseValidatorSuccessfullyConnected() throws ConnectionToRemoteDeviceException, IOException, JadbException {
       new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("connected to host:1");
    }

    @Test
    public void testProtocolResponseValidatorAlreadyConnected() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("already connected to host:1");
    }

    @Test(expected = ConnectionToRemoteDeviceException.class)
    public void testProtocolResponseValidatorErrorInValidate() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("some error occurred");
    }
}