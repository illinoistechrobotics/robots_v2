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
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventEnum;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Timer;

public class Penguin extends Controller{

	public enum PenguinControlEnum
	{
		INPUT_THRUST	(0),
		INPUT_PITCH		(1),
		INPUT_ROLL		(2),
		INPUT_YAW		(3),
		OFF_PITCH		(4),
		OFF_ROLL		(5),
		OFF_YAW			(6),
		OUTPUT_MOTORS	(7);
		
		private int value;
		private static final Map<Integer,PenguinControlEnum> lookup = new HashMap<Integer,PenguinControlEnum>();
	    static {
	    	for(PenguinControlEnum s : EnumSet.allOf(PenguinControlEnum.class))
	         lookup.put(s.getValue(), s);
	    }
	    public static PenguinControlEnum getPenguinEnum(int value){
	    	return lookup.get(value);
	    }
		private PenguinControlEnum(int v){
	    	this.value = v;
	    }
		public int getValue(){
	    	return this.value;
	    }
	}
	
	public enum PenguinPIDEnum
	{
		
		PITCH_P		(0),
		PITCH_I		(1),
		PITCH_D		(2),
		ROLL_P		(3),
		ROLL_I		(4),
		ROLL_D		(5),
		YAW_P		(6),
		YAW_I		(7),
		YAW_D		(8),
		PID_UPDATE_SUCCESS (0x10);
		
		private int value;
		private static final Map<Integer,PenguinPIDEnum> lookup = new HashMap<Integer,PenguinPIDEnum>();
	    static {
	    	for(PenguinPIDEnum s : EnumSet.allOf(PenguinPIDEnum.class))
	         lookup.put(s.getValue(), s);
	    }
	    public static PenguinPIDEnum getPIDEnum(int value){
	    	return lookup.get(value);
	    }
		private PenguinPIDEnum(int v){
	    	this.value = v;
	    }
		public int getValue(){
	    	return this.value;
	    }
	}
	
	public enum PenguinStateEnum
	{
		
		PITCH		(0),
		ROLL		(1),
		YAW			(2),
		MOT_0		(3),
		MOT_1		(4),
		MOT_2		(5),
		MOT_3		(6);
		
		private int value;
		private static final Map<Integer,PenguinStateEnum> lookup = new HashMap<Integer,PenguinStateEnum>();
	    static {
	    	for(PenguinStateEnum s : EnumSet.allOf(PenguinStateEnum.class))
	         lookup.put(s.getValue(), s);
	    }
	    public static PenguinStateEnum getStateEnum(int value){
	    	return lookup.get(value);
	    }
		private PenguinStateEnum(int v){
	    	this.value = v;
	    }
		public int getValue(){
	    	return this.value;
	    }
	}
	
	public Penguin(Queue q, Communication c, GUI d, Timer t){
		super(q,c,d,t);
	}
	
	@Override
	public void on_init(){
		super.on_init();
		timer.timer20hz = true;
	}
	
	@Override
	public void on_axis_change(Event ev){
		super.on_axis_change(ev);
		if(ev.getIndex() == 3){
			float scale = (ev.getValue()-127)/8*(-1);
			comm.sendEvent(new Event(EventEnum.VARIABLE, PenguinControlEnum.INPUT_PITCH.value, scale));
		}
		else if(ev.getIndex() == 2){
			float scale = (ev.getValue()-127)/8*(1);
			comm.sendEvent(new Event(EventEnum.VARIABLE, PenguinControlEnum.INPUT_ROLL.value, scale));
		}
		else if(ev.getIndex() == 0){
			float scale = (ev.getValue()-127)/8*(1);
			comm.sendEvent(new Event(EventEnum.VARIABLE, PenguinControlEnum.INPUT_YAW.value, scale));
		}
	}
	
	//These are set true if the button is pushed down and false when button is up
	//This is so that when the button is held down it continues to send new values 
	boolean throttle_up = false;
	boolean throttle_down = false;
	boolean yaw_off_plus = false;
	boolean yaw_off_neg = false;
	boolean pitch_off_plus = false;
	boolean pitch_off_neg = false;
	boolean roll_off_plus = false;
	boolean roll_off_neg = false;
	int output_motors = 0;
	
	@Override
	public void on_button_down(Event ev){
		super.on_button_down(ev);
		if(ev.getIndex() == 5){
			throttle_down = false;
			throttle_up = true;
		}
		else if(ev.getIndex() == 7){
			throttle_down = true;
			throttle_up = false;
		}
		else if(ev.getIndex() == 0){
			yaw_off_neg = false;
			yaw_off_plus = true;
		}
		else if(ev.getIndex() == 2){
			yaw_off_neg = true;
			yaw_off_plus = false;
		}
		else if(ev.getIndex() == 1){
			output_motors ^= 0x01;
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OUTPUT_MOTORS.value,output_motors));
		}
	}
	
	@Override
	public void on_button_up(Event ev){
		super.on_button_up(ev);
		if(ev.getIndex() == 5)
			throttle_up = false;
		else if(ev.getIndex() == 7)
			throttle_down = false;
		else if(ev.getIndex() == 0)
			yaw_off_plus = false;
		else if(ev.getIndex() == 2)
			yaw_off_neg = false;
	}
	
	@Override
	public void on_joy_hat(Event ev){
		super.on_joy_hat(ev);
		if(ev.getValue() == 3){
			//comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,10.0));
			pitch_off_plus = false;
			pitch_off_neg = true;
			roll_off_plus = false;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 7){
			//comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,-10.0));	
			pitch_off_plus = true;
			pitch_off_neg = false;
			roll_off_plus = false;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 5){
			//comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,10.0));	
			pitch_off_plus = false;
			pitch_off_neg = false;
			roll_off_plus = true;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 1){
			//comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,-10.0));	
			pitch_off_plus = false;
			pitch_off_neg = false;
			roll_off_plus = false;
			roll_off_neg = true;
		}
		else if(ev.getValue() == 0){
			pitch_off_plus = false;
			pitch_off_neg = false;
			roll_off_plus = false;
			roll_off_neg = false;
		}
	}
	
	@Override	
	public void on_gui(Event ev){
		super.on_gui(ev);
		if(ev.getIndex() == GUI.GUIEnum.PENGUIN_UPDATE_PID.value){
			try{
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.PITCH_P.value,Double.parseDouble(dis.txtPP.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.PITCH_I.value,Double.parseDouble(dis.txtPI.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.PITCH_D.value,Double.parseDouble(dis.txtPD.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.ROLL_P.value,Double.parseDouble(dis.txtRP.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.ROLL_I.value,Double.parseDouble(dis.txtRI.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.ROLL_D.value,Double.parseDouble(dis.txtRD.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.YAW_P.value,Double.parseDouble(dis.txtYP.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.YAW_I.value,Double.parseDouble(dis.txtYI.getText())));
				comm.sendEvent(new Event(EventEnum.PID,PenguinPIDEnum.YAW_D.value,Double.parseDouble(dis.txtYD.getText())));
			}catch(Exception e){
				System.out.println("ERROR - Cannot parse PID values.");
			}
		}
	}
	
	@Override
	public void on_20hz_timer(Event ev){
		super.on_20hz_timer(ev);
		if(throttle_up){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.INPUT_THRUST.value,10.0));
		}
		if(throttle_down){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.INPUT_THRUST.value,-10.0));
		}
		
		if(pitch_off_plus){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_PITCH.value,1.0));
		}
		if(pitch_off_neg){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_PITCH.value,-1.0));
		}
		if(roll_off_plus){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_ROLL.value,1.0));
		}
		if(roll_off_neg){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_ROLL.value,-1.0));
		}
		if(yaw_off_plus){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_YAW.value,1.0));
		}
		if(yaw_off_neg){
			comm.sendEvent(new Event(EventEnum.VARIABLE,PenguinControlEnum.OFF_YAW.value,-1.0));
		}
	}

	@Override
	public void on_heartbeat_timer(Event ev){
		super.on_heartbeat_timer(ev);
		if(heartbeat <= 5){
			dis.btnPenguinConnected.setBackground(Color.GREEN);
		}
		else{
			dis.btnPenguinConnected.setBackground(Color.RED);
		}
		
	}

	@Override
	public void on_imu(Event ev){
		super.on_imu(ev);
		DecimalFormat df = new DecimalFormat("#.##");
		if(ev.getIndex()==PenguinStateEnum.PITCH.value){
			dis.lblPitch.setText(df.format(ev.getFValue()));
			dis.sldPitch.setValue((int)ev.getFValue());
		}
		else if(ev.getIndex()==PenguinStateEnum.ROLL.value){
			dis.lblRoll.setText(df.format(ev.getFValue()));
			dis.sldRoll.setValue((int)ev.getFValue());
		}
		else if(ev.getIndex()==PenguinStateEnum.YAW.value){
			dis.lblYaw.setText(df.format(ev.getFValue()));
		}
		else if(ev.getIndex()==PenguinStateEnum.MOT_0.value){
			dis.lblMot1.setText(Integer.toString(ev.getValue()));
			dis.sldMot1.setValue(ev.getValue());
		}
		else if(ev.getIndex()==PenguinStateEnum.MOT_1.value){
			dis.lblMot2.setText(Integer.toString(ev.getValue()));
			dis.sldMot2.setValue(ev.getValue());
		}
		else if(ev.getIndex()==PenguinStateEnum.MOT_2.value){
			dis.lblMot3.setText(Integer.toString(ev.getValue()));
			dis.sldMot3.setValue(ev.getValue());
		}
		else if(ev.getIndex()==PenguinStateEnum.MOT_3.value){
			dis.lblMot4.setText(Integer.toString(ev.getValue()));
			dis.sldMot4.setValue(ev.getValue());
		}
	}
	
	@Override
	public void on_pid(Event ev){
		super.on_pid(ev);
		if(ev.getIndex()==PenguinPIDEnum.PID_UPDATE_SUCCESS.value){
			System.out.println("PID successfully updated."); 
		}
	}
	
	@Override
	public void on_shutdown(Event ev){
		super.on_shutdown(ev);
		dis.btnPenguinConnected.setBackground(Color.RED);
	}

}
