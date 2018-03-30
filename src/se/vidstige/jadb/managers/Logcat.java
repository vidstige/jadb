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
package se.vidstige.jadb.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

/**
 * Logcat interface to an Android device
 * 
 * @author Benoit Callebaut
 **/
 public class Logcat {
    private final JadbDevice device;
    private InputStream stream = null;

    /**
     *
     * @param device JadbDevice we want to get the logcat stream from
     */
    public Logcat(JadbDevice device) {
        this.device = device;
    }

    /**
     *
     * @return The raw byte stream of data from the logcat
     */
    public InputStream getStream() throws IOException, JadbException{
        if (stream == null)
            stream = new ProxyStream();
        return stream;
    }
    
    /**
     *  Closes the underlying stream if it was still opened.
     */
    public final void close(){
        if (stream != null)
            try{
                stream.close();
            }catch (Exception e){}
        stream = null;
    }
    
    /**
     *
     * @return true if the underlying stream is open. False if it was closed.
     */
    public final boolean isClosed(){
        return stream == null;
    } 
    
    /**
     *
     * @return The stream of data from the logcat encapsulated in a Buffered reader for convenience.
     * @throws IOException
     * @throws JadbException
     */
    public final BufferedReader getReader() throws IOException, JadbException{
        return new BufferedReader(new InputStreamReader(getStream()));
    }
    
    protected class ProxyStream extends InputStream{
        private InputStream backend;

        public ProxyStream() throws IOException, JadbException{
            backend = device.execute("exec", "logcat");
        }
        
        @Override
        public int read() throws IOException {
            if (backend == null)
                throw new IOException("Closed stream");
            return backend.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (backend == null)
                return 0;
            return backend.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (backend == null)
                return 0;
            return backend.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            if (backend == null)
                return 0;
            return backend.skip(n);
        }

        @Override
        public int available() throws IOException {
            if (backend == null)
                return 0;
            return backend.available();
        }

        @Override
        public void close() throws IOException {
            if (backend != null)
            backend.close();
            Logcat.this.stream = null;
        }

        @Override
        public synchronized void mark(int readlimit) {
            if (backend == null)
                return;
            backend.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            if (backend == null)
                return;
            backend.reset();
        }

        @Override
        public boolean markSupported() {
            if (backend == null)
                return false;
            return backend.markSupported();
        }
    }
}
