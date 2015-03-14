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
package org.illinoistechrobotics.controller;

import java.awt.Color;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventEnum;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Timer;

public class Modulus extends Controller{
	
	public Modulus(Queue q, Communication c, GUI d, Timer t){
		super(q,c,d,t);
	}
	
	@Override
	public void on_heartbeat_timer(Event ev){
		super.on_heartbeat_timer(ev);
		if(heartbeat <= 5){
			dis.btnModulusConnected.setBackground(Color.GREEN);
		}
		else{
			dis.btnModulusConnected.setBackground(Color.RED);
		}
	}
	
	@Override
	public void on_shutdown(Event ev){
		super.on_shutdown(ev);
		dis.btnModulusConnected.setBackground(Color.RED);
	}
	
	
	private int x1Axis = 0;
	private int x2Axis = 0;
	private int rAxis = 0;
	
	@Override
	public void on_axis_change(Event ev){
		int axis = ev.getIndex();
		
		if(axis == 1){
			x1Axis = ((ev.getValue() - 128)/3) * (-1);
			comm.sendEvent(new Event(EventEnum.MOTOR, 5, (int)x1Axis + 102));
			comm.sendEvent(new Event(EventEnum.MOTOR, 6, (int)x1Axis + 102));
		}
		else if(axis == 3){
			x2Axis = ((ev.getValue() - 128)/3) * (-1);
			comm.sendEvent(new Event(EventEnum.MOTOR, 7, (int)x2Axis + 102));
			comm.sendEvent(new Event(EventEnum.MOTOR, 8, (int)x2Axis + 102));
		}
		
		//double speed = Math.hypot(xAxis, yAxis);
		//speed = speed/5.0;
		
		//if(yAxis < 0){
		//	speed = speed * -1;
		//}

		//System.out.println(speed);
		
		
		
		
		//double strafe = Math.atan2(yAxis, xAxis)*255/(2*Math.PI);
		//System.out.println(strafe);
		
		//double front = strafe + rAxis;
		//double back = strafe - rAxis;
		
		//System.out.println(front + "," + back);
		
		//comm.sendEvent(new Event(EventEnum.MOTOR, 1, (int)front));
		//comm.sendEvent(new Event(EventEnum.MOTOR, 2, (int)front));
		//comm.sendEvent(new Event(EventEnum.MOTOR, 3, (int)back));
		//comm.sendEvent(new Event(EventEnum.MOTOR, 4, (int)back));
		
		//comm.sendEvent(new Event(EventEnum.MOTOR, 5, xAxis+102));
	}

	@Override
	public void on_button_down(Event ev){
		
		if(ev.getIndex() == 5){
			comm.sendEvent(new Event(EventEnum.SOLENOID, 1, 1));
		}
		if(ev.getIndex() == 7){
			comm.sendEvent(new Event(EventEnum.SOLENOID, 2, 1));
		}
		
	}
	
	@Override
	public void on_button_up(Event ev){
		if(ev.getIndex() == 5){
			comm.sendEvent(new Event(EventEnum.SOLENOID, 1, 0));
		}
		if(ev.getIndex() == 7){
			comm.sendEvent(new Event(EventEnum.SOLENOID, 2, 0));
		}
		
	}
	
	
}
