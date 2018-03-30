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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

/**
 *
 * @author samoa
 */
public class LogcatTest {
    
    public LogcatTest() {
    }

    protected Logcat findLogcat(){
         JadbConnection conn = new JadbConnection();
        JadbDevice dev = conn.getAnyDevice();
        if (dev == null){
            System.out.println("Unable to find a device for testing");
            return null;
        }
        
        return new Logcat(dev);
    }
    
    /**
     * Test of getStream method, of class Logcat.
     */
    @Test
    public void testGetStream() throws IOException, JadbException{
        System.out.println("getStream");
        
        Logcat instance = findLogcat();
        if (instance == null){
            System.out.println("Unable to open logcat");
        }
        
        InputStream result = instance.getStream();
        assertNotNull(result);
        
    }

    /**
     * Test of close method, of class Logcat.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        Logcat instance = findLogcat();
        if (instance == null){
            System.out.println("Unable to open logcat");
        }
        instance.close();
        
    }

    /**
     * Test of isClosed method, of class Logcat.
     */
    @Test
    public void testIsClosed() throws IOException, JadbException{
        System.out.println("isClosed");
        Logcat instance = findLogcat();
        if (instance == null){
            System.out.println("Unable to open logcat");
        }
        assertTrue(instance.isClosed());
        InputStream is = instance.getStream();
        assertFalse(instance.isClosed());
        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(LogcatTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue(instance.isClosed());
        
    }

    @Test
    public void testGetReader() throws Exception {
        System.out.println("getReader");
        Logcat instance = findLogcat();
        if (instance == null){
            System.out.println("Unable to open logcat");
        }
        BufferedReader result = instance.getReader();
        assertNotNull(result);
        assertFalse(instance.isClosed());
        
        Thread.sleep(1000);
        
        try{
            String line = result.readLine();
            for (int i = 0 ; i < 1000 && line != null ; i++)
                System.out.println(line = result.readLine());
        }catch (Exception e){}
    }
    
}
