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
package org.illinoistechrobotics.controller;

import java.awt.Color;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Timer;

public class SampleRobot extends Controller{

	public SampleRobot(Queue q, Communication c, GUI d, Timer t){
		super(q,c,d,t);
	}
	
	//Sample of all events
	//Don't need to include then all
	//just the ones you need
	//Please use the @Override annotation to make sure that the method will be called
	
	@Override
	public void on_init(){
		//init timer that will be used if any
	}
	
	@Override
	public void on_failsafe(){
		//place code to place the robot into a safe state
		//ie STOP all the motors and moving components
	}
	
	@Override
	public void on_heartbeat_timer(Event ev){
		//TODO:
		//Add code here for changing the connection status of robot
		//Example code
		if(heartbeat <= 5){
			dis.btnSampleConnected.setBackground(Color.GREEN);
		}
		else{
			dis.btnSampleConnected.setBackground(Color.RED);
		}
	}
	
	@Override
	public void on_shutdown(Event ev){
		//TODO:
		//Set status to disconnected
		//Example code
		dis.btnSampleConnected.setBackground(Color.RED);
	}

}
