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

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.illinoistechrobotics.common.Timer.TimerEnum;

public abstract class Robot extends Thread{

	public enum RobotEnum
	{
		
		ROSLUND		(0),
		FENRIR		(1),
		GOLIATH		(2),
		REAPER		(3),
		PENGUIN		(4),
		MONGOL		(5),
		GHOST		(6),
		MODULUS     (7),
		COMPUTER	(0xF0),
		UNKNOWN_ROBOT	(0xFF);
		
		private int value;
		private static final Map<Integer,RobotEnum> lookup = new HashMap<Integer,RobotEnum>();
	    static {
	    	for(RobotEnum s : EnumSet.allOf(RobotEnum.class))
	         lookup.put(s.getValue(), s);
	    }
	    public static RobotEnum getRobot(int value){
	    	RobotEnum temp = lookup.get(value);
	    	if(temp == null){
	    		return UNKNOWN_ROBOT;
	    	}
	    	return temp;
	    }
		private RobotEnum(int v){
	    	this.value = v;
	    }
		public int getValue(){
	    	return this.value;
	    }
	}
	
	protected Queue recv_q;
	protected Communication comm;
	protected GUI dis;
	protected Timer timer;
	
	public Robot(Queue q, Communication c, GUI d, Timer t){
		recv_q = q;	
		comm = c;
		dis = d;
		timer = t;
		on_init();
	}
	
	private volatile Boolean run = true;
	protected int heartbeat = 20;
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
			on_shutdown(new Event(EventEnum.ROBOT_EVENT_CMD_SHUTDOWN,0,0));
		}
	}
	
	public void run()
	{	
		recv_q.flush(); //clear the queue
		while(run){
			try{
				Event ev = recv_q.take();
				switch (ev.getCommand()){
				case ROBOT_EVENT_CMD:
					//break left out
				case ROBOT_EVENT_CMD_STOP:
					//break left out
				case ROBOT_EVENT_CMD_START:
					//break left out
				case ROBOT_EVENT_CMD_REBOOT:
					//break left out
				case ROBOT_EVENT_CMD_FAILSAFE:
					on_command_code(ev);
					break;
				case ROBOT_EVENT_CMD_HEARTBEAT:
					//need to update GUI
					heartbeat = 0;
					dis.btnGeneralStatus.setBackground(Color.GREEN);
    				dis.btnGeneralStatus.setText(Robot.RobotEnum.getRobot(ev.getIndex()).toString());
					on_heartbeat(ev);
					break;
				case ROBOT_EVENT_STATUS:
					on_status(ev);
					break;
				case ROBOT_EVENT_JOY_AXIS:
					on_axis_change(ev);
					break;
				case ROBOT_EVENT_JOY_BUTTON:
					if(ev.getValue() == 1)
						on_button_down(ev);
					else if (ev.getValue() == 0)
						on_button_up(ev);
					break;
				case ROBOT_EVENT_JOY_HAT:
					on_joy_hat(ev);
					break;
				case ROBOT_EVENT_JOY_STATUS:
					on_joy_status(ev);
					break;
				case ROBOT_EVENT_KEYBOARD:
					on_keyboard(ev);
					break;
				case ROBOT_EVENT_DISPLAY:
					on_display(ev);
					break;
				case ROBOT_EVENT_GUI:
					on_gui(ev);
					break;
				case ROBOT_EVENT_TIMER:
					if(ev.getIndex() == TimerEnum.TIMER_1HZ.value)
						on_1hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_10HZ.value)
						on_10hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_20HZ.value)
						on_20hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_25HZ.value)
						on_25hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_50HZ.value)
						on_50hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_100HZ.value)
						on_100hz_timer(ev);
					else if(ev.getIndex() == TimerEnum.TIMER_HEARTBEAT.value){
						if(heartbeat > 4){
    						dis.btnGeneralStatus.setBackground(Color.RED);
    	    				dis.btnGeneralStatus.setText("");
    					}
						on_heartbeat_timer(ev);
						comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_CMD_HEARTBEAT,RobotEnum.COMPUTER.getValue(),0));
						heartbeat++;
					}
					break;	
				case ROBOT_EVENT_MOTOR:
					on_motor(ev);
					break;
				case ROBOT_EVENT_SOLENOID:
					on_solenoid(ev);
					break;
				case ROBOT_EVENT_POSE:
					on_pose(ev);
					break;
				case ROBOT_EVENT_ADC:
					on_adc(ev);
					break;
				case ROBOT_EVENT_VARIABLE:
					on_variable(ev);
					break;
				case ROBOT_EVENT_IMU:
					on_imu(ev);
					break;
				case ROBOT_EVENT_PID:
					on_pid(ev);
					break;
				case ROBOT_EVENT_ENCODER:
					on_encoder(ev);
					break;
				case ROBOT_EVENT_EEPROM:
					on_eeprom(ev);
					break;
				case ROBOT_EVENT_IO:
					on_io(ev);
					break;
				case ROBOT_EVENT_CMD_SHUTDOWN:
					run = false;
					on_shutdown(ev);
					break;
				default:
					on_unknown_command(ev);
					break;
				}
			}
			catch(Exception e){}
		}
	}
	
	public abstract void on_init();
	public abstract void on_command_code(Event ev);
	public abstract void on_heartbeat(Event ev);
	public abstract void on_status(Event ev);
	public abstract void on_axis_change(Event ev);
	public abstract void on_button_down(Event ev);
	public abstract void on_button_up(Event ev);
	public abstract void on_joy_hat(Event ev);
	public abstract void on_joy_status(Event ev);
	public abstract void on_keyboard(Event ev);
	public abstract void on_display(Event ev);
	public abstract void on_gui(Event ev);
	public abstract void on_1hz_timer(Event ev);
	public abstract void on_10hz_timer(Event ev);
	public abstract void on_20hz_timer(Event ev);
	public abstract void on_25hz_timer(Event ev);
	public abstract void on_50hz_timer(Event ev);
	public abstract void on_100hz_timer(Event ev);
	public abstract void on_heartbeat_timer(Event ev);
	public abstract void on_motor(Event ev);
	public abstract void on_solenoid(Event ev);
	public abstract void on_pose(Event ev);
	public abstract void on_adc(Event ev);
	public abstract void on_variable(Event ev);
	public abstract void on_imu(Event ev);
	public abstract void on_pid(Event ev);
	public abstract void on_encoder(Event ev);
	public abstract void on_eeprom(Event ev);
	public abstract void on_io(Event ev);
	public abstract void on_shutdown(Event ev);
	public abstract void on_unknown_command(Event ev);
}
