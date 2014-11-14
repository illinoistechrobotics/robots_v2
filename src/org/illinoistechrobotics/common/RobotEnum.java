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
