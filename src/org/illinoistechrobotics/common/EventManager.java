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

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventEnum;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.RobotEnum;
import org.illinoistechrobotics.common.Timer;
import org.illinoistechrobotics.common.Timer.TimerEnum;

public abstract class EventManager extends Thread{
	
	protected Queue recv_q;
	protected Communication comm;
	protected Timer timer;
	protected RobotEnum robotEnum;
	
	public EventManager(Queue q, Communication c, Timer t, RobotEnum r){
		recv_q = q;	
		comm = c;
		timer = t;
		robotEnum = r;
		on_init();
	}
	
	protected volatile Boolean run = true;
	protected int heartbeat = 20;
	
	public void stopThread(){
		if(run != false){
			run = false;
			this.interrupt();
			on_shutdown(new Event(EventEnum.CMD_SHUTDOWN,0,0));
		}
	}
	
	@Override
	public void run()
	{	
		recv_q.flush(); //clear the queue
		while(run){
			try{
				Event ev = recv_q.take();
				switch (ev.getCommand()){
				case CMD:
					//break left out
				case CMD_STOP:
					//break left out
				case CMD_START:
					//break left out
				case CMD_REBOOT:
					//break left out
				case CMD_FAILSAFE:
					on_command_code(ev);
					break;
				case CMD_HEARTBEAT:
					//need to update GUI
					heartbeat = 0;
					on_heartbeat(ev);
					break;
				case STATUS:
					on_status(ev);
					break;
				case JOY_AXIS:
					on_axis_change(ev);
					break;
				case JOY_BUTTON:
					if(ev.getValue() == 1)
						on_button_down(ev);
					else if (ev.getValue() == 0)
						on_button_up(ev);
					break;
				case JOY_HAT:
					on_joy_hat(ev);
					break;
				case JOY_STATUS:
					on_joy_status(ev);
					break;
				case KEYBOARD:
					on_keyboard(ev);
					break;
				case DISPLAY:
					on_display(ev);
					break;
				case GUI:
					on_gui(ev);
					break;
				case TIMER:
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
    						on_failsafe();
    					}
						on_heartbeat_timer(ev);
						comm.sendEvent(new Event(EventEnum.CMD_HEARTBEAT,robotEnum.getValue(),0));
						heartbeat++;
					}
					break;	
				case MOTOR:
					on_motor(ev);
					break;
				case SOLENOID:
					on_solenoid(ev);
					break;
				case POSE:
					on_pose(ev);
					break;
				case ADC:
					on_adc(ev);
					break;
				case VARIABLE:
					on_variable(ev);
					break;
				case IMU:
					on_imu(ev);
					break;
				case PID:
					on_pid(ev);
					break;
				case ENCODER:
					on_encoder(ev);
					break;
				case EEPROM:
					on_eeprom(ev);
					break;
				case IO:
					on_io(ev);
					break;
				case CMD_SHUTDOWN:
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
	
	public void on_init(){}
	public void on_failsafe(){}
	public void on_command_code(Event ev){}
	public void on_heartbeat(Event ev){}
	public void on_status(Event ev){}
	public void on_axis_change(Event ev){}
	public void on_button_down(Event ev){}
	public void on_button_up(Event ev){}
	public void on_joy_hat(Event ev){}
	public void on_joy_status(Event ev){}
	public void on_keyboard(Event ev){}
	public void on_display(Event ev){}
	public void on_gui(Event ev){}
	public void on_1hz_timer(Event ev){}
	public void on_10hz_timer(Event ev){}
	public void on_20hz_timer(Event ev){}
	public void on_25hz_timer(Event ev){}
	public void on_50hz_timer(Event ev){}
	public void on_100hz_timer(Event ev){}
	public void on_heartbeat_timer(Event ev){}
	public void on_motor(Event ev){}
	public void on_solenoid(Event ev){}
	public void on_pose(Event ev){}
	public void on_adc(Event ev){}
	public void on_variable(Event ev){}
	public void on_imu(Event ev){}
	public void on_pid(Event ev){}
	public void on_encoder(Event ev){}
	public void on_eeprom(Event ev){}
	public void on_io(Event ev){}
	public void on_shutdown(Event ev){}
	public void on_unknown_command(Event ev){}
}
