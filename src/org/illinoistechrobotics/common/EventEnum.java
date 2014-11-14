/*
Copyright 2014 (c) Illinois Tech Robotics <robotics.iit@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.illinoistechrobotics.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EventEnum {
    CMD                 (0x00), // Commands  
    CMD_START			(0x01),
    CMD_STOP			(0x02),
    CMD_REBOOT			(0x03),
    CMD_SHUTDOWN		(0x04),
    CMD_FAILSAFE		(0x05),
    CMD_HEARTBEAT		(0x06),
    
    STATUS              (0x10), // Status information
    
    JOY_AXIS            (0x20), // Joystick movements
    JOY_BUTTON          (0x21), // Button presses
    JOY_HAT				(0x22), // D-pad pressed
    JOY_STATUS			(0x23), // Joystick status
    
    KEYBOARD			(0x30), // Keyboard Events
    
    DISPLAY				(0x40), // Display info events
    GUI					(0x41), // GUI event i.e. Button pushes
    TIMER               (0x50), // Timer events
    MOTOR               (0x60), // Motor events
    SOLENOID			(0x70), // Solenoid events for pneumatics and relays
    POSE				(0x80), // Pose events for states
    ADC                 (0x90), // ADC events
    VARIABLE            (0xA0), // Variable events
    IMU		            (0xB0), // IMU events
    PID					(0xB1), // PID events
	ENCODER				(0xC0), // Encoder events
    EEPROM				(0xD0), // Send data to be stored in eeprom
    IO					(0xE0), // Send generic IO events
    UNKNOWN_EVENT		(0xFF); // Unknown Event
    
    // Feel free to add more commands but set different values. 
    // Try to do it with the available commands first 
    // Don't remove events please
    // Don't forget to add the new events to any other depenency code
    
    private int value;
    
    /**
     * This is for reverse lookup 
     * If you have a value it will return the Enum
     */
    private static final Map<Integer,EventEnum> lookup = new HashMap<Integer,EventEnum>();
    static {
    	for(EventEnum s : EnumSet.allOf(EventEnum.class))
         lookup.put(s.getValue(), s);
    }
    public static EventEnum getEvent(int value){
    	EventEnum temp = lookup.get(value);
    	if(temp == null){
    		return UNKNOWN_EVENT;
    	}
    	return temp;
    }
        
    private EventEnum(int v){
    	this.value = v;
    }
    
    public int getValue(){
    	return this.value;
    }
    
}
