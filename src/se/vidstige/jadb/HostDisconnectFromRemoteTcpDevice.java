package se.vidstige.jadb;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HostDisconnectFromRemoteTcpDevice {
    private final Transport transport;
    private final ResponseValidator responseValidator;

    HostDisconnectFromRemoteTcpDevice(Transport transport) {
        this.transport = transport;
        this.responseValidator = new ResponseValidatorImp();
    }

    //Visible for testing
    HostDisconnectFromRemoteTcpDevice(Transport transport, ResponseValidator responseValidator) {
        this.transport = transport;
        this.responseValidator = responseValidator;
    }

    InetSocketAddress disconnect(InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        transport.send(String.format("host:disconnect:%s:%d", inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        verifyTransportLevel();
        verifyProtocolLevel();

        return inetSocketAddress;
    }

    private void verifyTransportLevel() throws IOException, JadbException {
        transport.verifyResponse();
    }

    private void verifyProtocolLevel() throws IOException, ConnectionToRemoteDeviceException {
        String status = transport.readString();
        responseValidator.validate(status);
    }

    //@VisibleForTesting
    interface ResponseValidator {
        void validate(String response) throws ConnectionToRemoteDeviceException;
    }

    final static class ResponseValidatorImp implements ResponseValidator {
        private final static String SUCCESSFULLY_DISCONNECTED = "disconnected";
        private final static String ALREADY_DISCONNECTED = "error: no such device";


        ResponseValidatorImp() {
        }

        public void validate(String response) throws ConnectionToRemoteDeviceException {
            if (!checkIfConnectedSuccessfully(response) && !checkIfAlreadyConnected(response)) {
                throw new ConnectionToRemoteDeviceException(extractError(response));
            }
        }

        private boolean checkIfConnectedSuccessfully(String response) {
            return response.startsWith(SUCCESSFULLY_DISCONNECTED);
        }

        private boolean checkIfAlreadyConnected(String response) {
            return response.startsWith(ALREADY_DISCONNECTED);
        }

        private String extractError(String response) {
            int lastColon = response.lastIndexOf(":");
            if (lastColon != -1) {
                return response.substring(lastColon, response.length());
            } else {
                return response;
            }
        }
    }
}
