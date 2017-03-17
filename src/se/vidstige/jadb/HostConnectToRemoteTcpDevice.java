package se.vidstige.jadb;

import se.vidstige.jadb.entities.TcpAddressEntity;

import java.io.IOException;

public class HostConnectToRemoteTcpDevice {
    private final Transport transport;

    public HostConnectToRemoteTcpDevice(Transport transport) {
        this.transport = transport;
    }

    public TcpAddressEntity connect(TcpAddressEntity tcpAddressEntity)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        transport.send(String.format("host:connect:%s:%d", tcpAddressEntity.getHost(), tcpAddressEntity.getPort()));
        verifyProtocolLevel();
        verifyCommandLevel();

        return tcpAddressEntity;
    }

    private void verifyProtocolLevel() throws IOException, JadbException {
        transport.verifyResponse();
    }

    private void verifyCommandLevel() throws IOException, ConnectionToRemoteDeviceException {
        String status = transport.readString();
        new ResponseValidator(status).validate();
    }

    final static class ResponseValidator {
        private final static String SUCCESSFULLY_CONNECTED = "connected to";
        private final static String ALREADY_CONNECTED = "already connected to";

        private final String response;

        public ResponseValidator(String response) {
            this.response = response;
        }

        public void validate() throws ConnectionToRemoteDeviceException {
            if(!checkIfConnectedSuccessfully() && !checkIfAlreadyConnected()) {
                throw new ConnectionToRemoteDeviceException(extractError());
            }
        }

        private boolean checkIfConnectedSuccessfully()  {
            return response.contains(SUCCESSFULLY_CONNECTED);
        }

        private boolean checkIfAlreadyConnected()  {
            return response.equals(ALREADY_CONNECTED);
        }

        private String extractError() {
            int lastColon = response.lastIndexOf(":");
            if(lastColon != -1) {
                return response.substring(lastColon, response.length());
            } else {
                return response;
            }
        }
    }
}
