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

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventManager;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.RobotEnum;
import org.illinoistechrobotics.common.Timer;

public class Modulus extends EventManager{
	
	public Modulus(Queue q, Communication c, Timer t){
		super(q,c,t,RobotEnum.MODULUS);
	}
	
	@Override
	public void on_axis_change(Event ev){
		System.out.println(ev.toString());
	}

}
