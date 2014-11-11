/*
Copyright 2013 (c) Illinois Tech Robotics <robotics.iit@gmail.com>

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

import java.util.ArrayList;
import java.util.List;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.DirectInputEnvironmentPlugin;
import net.java.games.input.EventQueue;

public class Joystick extends Thread{
	private Queue queue = null;
	private Controller joy = null;
	private volatile Boolean run = true;
	private String name = null;
	
	public Joystick(Queue q){
		this.queue = q;
	}
	
	/**
	 * returns the first Joystick or Gamepad 
	 */
	public static List<String> getJoystickNames(){ 
		List<Controller> cs = Joystick.getJoysticks();
		List<String> csName = new ArrayList<String>();

		for(int i=0; i<cs.size(); i++){
			csName.add(i + " " + cs.get(i).getName()); 
		}
		
		return csName; 
	}
	
	public static List<Controller> getJoysticks(){
		List<Controller> csList = new ArrayList<Controller>();
		Controller[] cs;
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			DirectInputEnvironmentPlugin diep = new DirectInputEnvironmentPlugin();
			cs = diep.getControllers();
		}
		else {
			ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment(); 
			cs = ce.getControllers(); 
		}
		for(int i=0; i<cs.length; i++){
			//this will only output the controllers
			if(cs[i].getType() == Controller.Type.STICK || cs[i].getType() == Controller.Type.GAMEPAD){
				csList.add( cs[i]);
			}
		}
		return csList; 
	}
	
	public void listen(String s) {
		List<Controller> cs = Joystick.getJoysticks();
		for(int i=0; i<cs.size(); i++){
			if(s!=null && s.equals(i + " " + cs.get(i))){
				joy=cs.get(i);
				name = s;
				break;
			}
		}
		if(joy != null){
			//clear any joystick events in the queue
			EventQueue event_q = joy.getEventQueue();
			net.java.games.input.Event joy_event = new net.java.games.input.Event();
			while(event_q.getNextEvent(joy_event)){
				;
			}
			updateJoystickAll();
			this.start();
		}
	}
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
			joy = null;
			name = null;
		}
	}
	
	/**
	 * "listens in" on the joystick and adds event if there was a change
	 * populates the Robot_Event with proper command, index, and value.
	 * axis index (X=0,Y=1,X1=2,Y1=3) value 0-255 up and right is 255, down and left is 0
	 * buttons index equals button number-1 i.e(1 on controller has index 0) value 0 released(up) 1 pressed(down)
	 * d_pad(hat) index 0, value 0 neutral 1-8 (1 starting a 9 o'clock position and moving clockwise)
	 * also updates the gui with joystick values hack for linux/mac since drivers label things differently
	 */
	public void run(){
		Event ev = new Event();
		run = true;
		//clear any joystick events in the queue
		EventQueue event_q = joy.getEventQueue();
		net.java.games.input.Event joy_event = new net.java.games.input.Event();
		while(event_q.getNextEvent(joy_event)){
			;
		}
		
		while(run){
			try{
				if(joy.poll()==false){
					Event joy_ev = new Event(EventEnum.ROBOT_EVENT_JOY_STATUS,(short)0,0);
					queue.put(joy_ev);
					return;
				}
			}
			catch(Exception e){
				System.err.println("Can not open joystick");
				return;
			}
			while(event_q.getNextEvent(joy_event)){
				Component comp = joy_event.getComponent();						
				String command = comp.getName();
				if(command.equals("X Axis") || command.equals("x")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)0);
					ev.setValue((int)((joy_event.getValue() + 1.0) * (255.0) / (2.0))); //convert from 1.0 to -1.0 to 255 to 0
				}
				else if(command.equals("Y Axis") || command.equals("y")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)1);
					ev.setValue((int)((joy_event.getValue() - 1.0) * (-255.0) / (2.0))); //inverts the value up 255 down 0
				}
				else if(command.equals("Z Axis") || command.equals("z")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)2);
					ev.setValue((int)((joy_event.getValue() + 1.0) * (255.0) / (2.0)));
				}
				else if(command.equals("Z Rotation")|| command.equals("rz")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
					ev.setIndex((short)3);
					ev.setValue((int)((joy_event.getValue() - 1.0) * (-255.0) / (2.0)));
				}
				else if(command.contains("Button")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)Integer.parseInt(command.substring(command.indexOf(' ')+1)));
					ev.setValue((int)joy_event.getValue());  
				}
				else if(command.equals("Trigger")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)0);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Thumb")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)1);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Thumb 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)2);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Top")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)3);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Top 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)4);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Pinkie")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)5);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)6);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base 2")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)7);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base 3")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)8);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base 4")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)9);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base 5")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)10);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Base 6")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
					ev.setIndex((short)11);
					ev.setValue((int)joy_event.getValue());
				}
				else if(command.equals("Hat Switch") || command.equals("pov")){
					ev.setCommand(EventEnum.ROBOT_EVENT_JOY_HAT);
					ev.setIndex((short)0);
					float val = joy_event.getValue();
					if(val == 0.0){
						ev.setValue(0);
					}
					else if(val == 0.125){
						ev.setValue(2);
					}
					else if(val == .25){
						ev.setValue(3);
					}
					else if(val == .375){
						ev.setValue(4);
					}
					else if(val == .5){
						ev.setValue(5);
					}
					else if(val == .625){
						ev.setValue(6);
					}
					else if(val == .75){
						ev.setValue(7);
					}
					else if(val == .875){
						ev.setValue(8);
					}
					else if(val == 1.0){
						ev.setValue(1);
					}
				}
				try{
					//check to see if there is no duplicate joy event if so override 
					//if we don't override we get overridden with joystick events and there is a backlog of events
					if(ev.getCommand()==EventEnum.ROBOT_EVENT_JOY_AXIS){
						queue.putOverride(ev);
					}
					else{
						queue.put(ev);
					}
				}
				catch(Exception e){
					System.out.println("Error");
				}
			}
			
			try{
				Thread.sleep(10);
			}
			catch(Exception e){	
			}
			
		}
	}
	
	//this is used only at start up to get all initial values otherwise it shouldn't be called
	private void updateJoystickAll(){
		joy.poll();	
		Event ev = new Event();
		ev.setCommand(EventEnum.ROBOT_EVENT_JOY_AXIS);
		ev.setIndex((short)0);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.X).getPollData() - 1.0) * (-255.0) / (2.0)));
		queue.put(ev);
		ev.setIndex((short)1);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.Y).getPollData() - 1.0) * (-255.0) / (2.0)));
		queue.put(ev);
		ev.setIndex((short)2);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.Z).getPollData() - 1.0) * (-255.0) / (2.0)));
		queue.put(ev);
		ev.setIndex((short)3);
		ev.setValue((int)((joy.getComponent(Identifier.Axis.RZ).getPollData() - 1.0) * (-255.0) / (2.0)));
		queue.put(ev);
		
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
			ev.setIndex((short)0);
			ev.setValue((int)(joy.getComponent(Identifier.Button._0).getPollData()));
			queue.put(ev);
			ev.setIndex((short)1);
			ev.setValue((int)(joy.getComponent(Identifier.Button._1).getPollData()));
			queue.put(ev);
			ev.setIndex((short)2);
			ev.setValue((int)(joy.getComponent(Identifier.Button._2).getPollData()));
			queue.put(ev);
			ev.setIndex((short)3);
			ev.setValue((int)(joy.getComponent(Identifier.Button._3).getPollData()));
			queue.put(ev);
			ev.setIndex((short)4);
			ev.setValue((int)(joy.getComponent(Identifier.Button._4).getPollData()));
			queue.put(ev);
			ev.setIndex((short)5);
			ev.setValue((int)(joy.getComponent(Identifier.Button._5).getPollData()));
			queue.put(ev);
			ev.setIndex((short)6);
			ev.setValue((int)(joy.getComponent(Identifier.Button._6).getPollData()));
			queue.put(ev);
			ev.setIndex((short)7);
			ev.setValue((int)(joy.getComponent(Identifier.Button._7).getPollData()));
			queue.put(ev);
			ev.setIndex((short)8);
			ev.setValue((int)(joy.getComponent(Identifier.Button._8).getPollData()));
			queue.put(ev);
			ev.setIndex((short)9);
			ev.setValue((int)(joy.getComponent(Identifier.Button._9).getPollData()));
			queue.put(ev);
			ev.setIndex((short)10);
			ev.setValue((int)(joy.getComponent(Identifier.Button._10).getPollData()));
			queue.put(ev);
			ev.setIndex((short)11);
			ev.setValue((int)(joy.getComponent(Identifier.Button._11).getPollData()));
			queue.put(ev);
		}
		else if(System.getProperty("os.name").contains("nux")){
			ev.setCommand(EventEnum.ROBOT_EVENT_JOY_BUTTON);
			ev.setIndex((short)0);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TRIGGER).getPollData()));
			queue.put(ev);
			ev.setIndex((short)1);
			ev.setValue((int)(joy.getComponent(Identifier.Button.THUMB).getPollData()));
			queue.put(ev);
			ev.setIndex((short)2);
			ev.setValue((int)(joy.getComponent(Identifier.Button.THUMB2).getPollData()));
			queue.put(ev);
			ev.setIndex((short)3);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TOP).getPollData()));
			queue.put(ev);
			ev.setIndex((short)4);
			ev.setValue((int)(joy.getComponent(Identifier.Button.TOP2).getPollData()));
			queue.put(ev);
			ev.setIndex((short)5);
			ev.setValue((int)(joy.getComponent(Identifier.Button.PINKIE).getPollData()));
			queue.put(ev);
			ev.setIndex((short)6);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE).getPollData()));
			queue.put(ev);
			ev.setIndex((short)7);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE2).getPollData()));
			queue.put(ev);
			ev.setIndex((short)8);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE3).getPollData()));
			queue.put(ev);
			ev.setIndex((short)9);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE4).getPollData()));
			queue.put(ev);
			ev.setIndex((short)10);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE5).getPollData()));
			queue.put(ev);
			ev.setIndex((short)11);
			ev.setValue((int)(joy.getComponent(Identifier.Button.BASE6).getPollData()));
			queue.put(ev);
		}
		
		ev.setCommand(EventEnum.ROBOT_EVENT_JOY_HAT);
		ev.setIndex((short)0);
		
		float val = joy.getComponent(Identifier.Axis.POV).getPollData();
		if(val == 0.0){
			ev.setValue(0);
		}
		else if(val == 0.125){
			ev.setValue(2);
		}
		else if(val == .25){
			ev.setValue(3);
		}
		else if(val == .375){
			ev.setValue(4);
		}
		else if(val == .5){
			ev.setValue(5);
		}
		else if(val == .625){
			ev.setValue(6);
		}
		else if(val == .75){
			ev.setValue(7);
		}
		else if(val == .875){
			ev.setValue(8);
		}
		else if(val == 1.0){
			ev.setValue(1);
		}
		queue.put(ev);
	}
	
	public Controller getJoy(){
		return joy;
	}
	
	public String getJoyName(){
		return name;
	}
	
	public boolean checkJoystick(){	
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			DirectInputEnvironmentPlugin diep = new DirectInputEnvironmentPlugin();
			Controller[] cs = diep.getControllers();
			int j=0;
			for(int i=0; i<cs.length; i++){
				//this will only output the controllers
				if(cs[i].getType() == Controller.Type.STICK || cs[i].getType() == Controller.Type.GAMEPAD){
					j++;
				}
			}
		
			if(j==0){
				Event ev = new Event(EventEnum.ROBOT_EVENT_JOY_STATUS,(short)0,0);
				queue.put(ev);
				this.stopThread();
				return false;
			}
			return true;
		}
		return true;
	}
}
