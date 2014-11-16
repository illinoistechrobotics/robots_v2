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
package org.illinoistechrobotics.robot;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Ethernet;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventEnum;
import org.illinoistechrobotics.common.EventManager;
import org.illinoistechrobotics.common.Joystick;
import org.illinoistechrobotics.common.Keyboard;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Serial;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class RobotMain {
	
	@Option(name="-sp", aliases={"--serialPort"}, usage="serial communication port name")
	private String serialPort = null;
	
	@Option(name="-sb", aliases={"--serialBaud"}, usage="serial communication port baud rate")
	private int serialBaud = 0;
	
	@Option(name="-esp", aliases={"--ethernetServerPort"}, usage="ethernet server port")
	private int ethernetServerPort = 0;
	
	@Option(name="-ecp", aliases={"--ethernetClientPort"}, usage="ethernet client port")
	private int ethernetListenerPort = 0;
	
	@Option(name="-ip", aliases={"--ipAddress"}, usage="report ip address")
	private String ipAddress = null;

	@Option(name="-r", aliases={"--robot"}, usage="robot that will be running")
	private String robotName = "Unknown";
	
	@Option(name="-j", aliases={"--joy"}, usage="name of joy stick to use")
	private String joyName = null;
	
	@Option(name="-k", aliases={"--key"}, usage="name of joy stick to use")
	private boolean bKey = false;
	
	@Option(name="-l", aliases={"--list"}, usage="list the available serail ports and joy sticks")
	private boolean list = false;
	
	@Option(name="-h", aliases={"--help"}, usage="help command to print this message", help=true)
	
	@Argument
    private List<String> arguments = new ArrayList<String>();
	
	private Timer trDeviceChecker;
	private Queue queue = new Queue(1000);
	private org.illinoistechrobotics.common.Timer timer = new org.illinoistechrobotics.common.Timer(queue);
	private Serial serial = new Serial(queue);
	private Ethernet ethernet = new Ethernet(queue);
	private Joystick joy = new Joystick(queue);
	private Keyboard key = new Keyboard(queue);
	Communication comm = null;
	
	public RobotMain(String[] args) {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() { 
		    	exit();
		    }
		});
		
		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
			if(serialPort != null){
				if(serialBaud == 0){
					throw new CmdLineException(parser, "If serial port specified baud rate needs to be specifed as well", null);
				}
			}
			if(ipAddress != null){
				if(ethernetServerPort == 0 || ethernetListenerPort == 0){
					throw new CmdLineException(parser, "If ip address specified srever and listener ports needs to be specifed as well", null);
				}
			}
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -jar robot.jar [options...] arguments...");
			parser.printUsage(System.err);
			System.exit(1);
		}
		
		if(list){
			//list  serial ports and joy sticks
			List<CommPortIdentifier> commPortIdentifiers = Serial.getSerialPorts();
			System.out.println("Comm Ports");
			for(CommPortIdentifier c:commPortIdentifiers){
				System.out.println(c.getName());
			}
			System.out.println();
			System.out.println("Controllers");
			List<String> controllers = Joystick.getJoystickNames();
			for(String c:controllers){
				System.out.println(c);
			}
			System.exit(0);
		}
		
		if(robotName == null){
			System.err.println("Option \"-r (--robot)\" is required");
			parser.printUsage(System.err);
			System.exit(1);
		}
		
		if(serialPort!=null){
			if(serial.openSerial(serialBaud, serialPort)){
				System.out.println("Serial port " + serialPort + " at " + serialBaud + " baud.");
				comm = serial;
			}
		}
		
		if(ipAddress!=null){
			if(ethernet.connect(ipAddress, ethernetServerPort, ethernetServerPort)){
				System.out.println("Listening on ethernet port " + ethernetListenerPort + " and sending messages to " + ipAddress + ":" + ethernetServerPort + ".");
				comm = ethernet;
			}
		}
		
		if(comm == null){
			System.err.println("No serial port or ethernet port was opened.");
			System.exit(1);
		}
		
		if(joyName!=null){
			joy.listen(joyName);
			System.out.println("Joystick " + joyName + " is opened.");
		}
		
		if(bKey){
			key.listen();
			System.out.println("Listening for keyboard events.");
		}
		
		timer.start();
		
		EventManager robot = null;
		
		//TODO:
		//Add new if else for each robot
		if("Modulus".equals(robotName)){ //did reverse way so don't need to worry about null pointer
			System.out.println("Creating Modulus Robot.");
			robot = new Modulus(queue, comm, timer);
		}
		else if("SampleRobot".equals(robotName)){
			System.out.println("Creating Sample Robot.");
			robot = new SampleRobot(queue, comm, timer);
		}
		else{
			System.err.println("Robot " + robot + " is not a valid robot name");
			System.exit(1);
		}
		
		trDeviceChecker = new Timer();
		trDeviceChecker.schedule(new deviceChecker(), 0, 1000);
		robot.start();
		
	}
	
	public class deviceChecker extends TimerTask{
	    
		@Override
        public void run(){
        	try{
        		List<CommPortIdentifier> com = Serial.getSerialPorts();
        		boolean serialConnected = false;
        		for(int i=0; i<com.size(); i++){ 
    				if(serial.isOpen() && serial.getName().equals(com.get(i).getName())){
    					serialConnected = true;
    				}	
    			}
        		if(!serialConnected){
					serial.closeSerial();
				}
        		else if(!serial.isOpen()){
        			if(Serial.getSerialPorts().contains(serialPort)){
        				serial.openSerial(serialBaud, serialPort);
        			}
        		}
        		
        		List<String> con = Joystick.getJoystickNames();
        		boolean joyConnected = false;
    			for(int i=0; i<con.size(); i++){
    				if(joy.getJoy() != null){
    					joyConnected = true;
    				}
    			}
    			if(!joyConnected){
					joy.stopThread();
					joy = new Joystick(queue);
				}
        		else if(!serial.isOpen()){
        			if(Joystick.getJoystickNames().contains(joyName)){
        				joy.listen(joyName);
        			}
        		}      		
        	}
        	catch(Exception e){
        		e.printStackTrace();
       		}
        }
    }
	
	public void exit(){
		
		if(comm != null){
			comm.sendEvent(new Event(EventEnum.CMD_SHUTDOWN, (short)0, 0));
			try{
	        	Thread.sleep(1000);
	        } catch(InterruptedException e) {	
	        }
		}
		
		if(serial.isOpen()){
			serial.closeSerial();
		}
    	
    	if(ethernet.isConnected() == true){
			ethernet.stopThread();
		}
    	
    	if(joy.getJoy() != null){
			joy.stopThread();
		}
    	
    	if(key.isListening()){
			key.stop();
		}
    	
    	if(trDeviceChecker != null){
    		trDeviceChecker.cancel();
    	}
    	
        try{
        	Thread.sleep(1000);
        } catch(InterruptedException e) {	
        }
        
        System.out.println("Shutting Down");
   	}
	
	public static void main(String[] args) {
		new RobotMain(args);
	}
	
}
