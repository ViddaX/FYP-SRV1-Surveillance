/*
 * SensorSampler.java
 *
 * Copyright (c) 2008-2010 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This class is based heavily on the 'on SPOT' portion of
 * the SendDataDemo included with the Sun SPOT software. It has been modified 
 * to remove the unneeded code and repeatedly send the accelerometer X value
 * to the basestation.
 *
 * @author: Vipul Gupta
 * modified: Ron Goldman
 * Modified: David Dixon
 */
public class SensorSampler extends MIDlet {
    /** Port used to communicate with host basestation  */
    private static final int HOST_PORT = 67;
    /** time to wait between broadcasts */
    private static final int WAIT_PERIOD = 5*1000;  
    
    /**
     *Runs when device is turned on, repeatedly sends Accelerometer X value
     */
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram datagram = null;
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
        IAccelerometer3D accel = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
	
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            datagram = rCon.newDatagram(50);  
        } catch (Exception e) {
            System.err.println("Error creating connection: " +e);
            notifyDestroyed();
        }
        
        while (true) {
            try {
                long now = System.currentTimeMillis();
                int tiltX = (int)Math.toDegrees(accel.getTiltX()); // returns [-90, +90]
                int offset = -tiltX / 15; // so bubble goes to higher side [6, -6]
                if (offset < -3) offset = -3; // clip angle to range [3, -3]
                if (offset > 3) offset = 3;
     
                led.setRGB(255, 255, 255);
                led.setOn();
                Utils.sleep(50);
                led.setOff();
                
                datagram.reset();
                datagram.writeInt(tiltX);
                rCon.send(datagram);
                    
                Utils.sleep(WAIT_PERIOD - (System.currentTimeMillis() - now));
            } catch (Exception e) {
                System.out.println("Error receiving/sending data: " +e);
            }
        }
    }
    
    protected void pauseApp() {
       //Never called by the Squawk VM in this implementation
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
          //called if startup throws any exception other than MIDletStateChangeException
    }
    
}