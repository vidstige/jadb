/*
 * Copyright 2018 samoa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.vidstige.jadb;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author samoa
 */
public class SocketTransportFactory implements ITransportFactory{

    private final String host;
    private final int port;
    
    public static final int DEFAULTPORT = 5037;

    public SocketTransportFactory() {
        this("localhost", DEFAULTPORT);
    }
    
    public SocketTransportFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public Transport createTransport() throws IOException {
        return new Transport(new Socket(host, port));
    }
    
}
