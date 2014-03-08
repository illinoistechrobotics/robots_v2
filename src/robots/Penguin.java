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
package robots;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import common.Communication;
import common.Event;
import common.EventEnum;
import common.GUI;
import common.Queue;
import common.Robot;
import common.Robot.RobotEnum;
import common.Timer;

public class Penguin extends Robot{

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
		super(q,c,d, t);
		t.timer20hz = true;
	}
	
	public void on_command_code(Event ev){
		
	}
	
	public void on_heartbeat(Event ev) {
		
	}
	
	public void on_status(Event ev){
		
	}
	
	public void on_axis_change(Event ev){
		if(ev.getIndex() == 3){
			float scale = (ev.getValue()-127)/16*(-1);
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE, PenguinControlEnum.INPUT_PITCH.value, scale));
		}
		else if(ev.getIndex() == 2){
			float scale = (ev.getValue()-127)/16*(-1);
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE, PenguinControlEnum.INPUT_ROLL.value, scale));
		}
		else if(ev.getIndex() == 0){
			float scale = (ev.getValue()-127)/16*(-1);
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE, PenguinControlEnum.INPUT_YAW.value, scale));
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
	public void on_button_down(Event ev){
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
	}
	
	public void on_button_up(Event ev){
		if(ev.getIndex() == 5)
			throttle_up = false;
		else if(ev.getIndex() == 7)
			throttle_down = false;
		else if(ev.getIndex() == 0)
			yaw_off_plus = false;
		else if(ev.getIndex() == 2)
			yaw_off_neg = false;
	}
	
	public void on_joy_hat(Event ev){
		if(ev.getValue() == 3){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,10.0));
			pitch_off_plus = true;
			pitch_off_neg = false;
			roll_off_plus = false;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 7){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,-10.0));	
			pitch_off_plus = false;
			pitch_off_neg = true;
			roll_off_plus = false;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 5){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,10.0));	
			pitch_off_plus = false;
			pitch_off_neg = false;
			roll_off_plus = true;
			roll_off_neg = false;
		}
		else if(ev.getValue() == 1){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,-10.0));	
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
	
	public void on_joy_status(Event ev){
		
	}
	
	public void on_keyboard(Event ev){
		
	}
	
	public void on_display(Event ev){
		
	}
	
	public void on_gui(Event ev){
		if(ev.getIndex() == GUI.GUIEnum.PENGUIN_UPDATE_PID.value){
			try{
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.PITCH_P.value,Double.parseDouble(dis.txtPP.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.PITCH_I.value,Double.parseDouble(dis.txtPI.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.PITCH_D.value,Double.parseDouble(dis.txtPD.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.ROLL_P.value,Double.parseDouble(dis.txtRP.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.ROLL_I.value,Double.parseDouble(dis.txtRI.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.ROLL_D.value,Double.parseDouble(dis.txtRD.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.YAW_P.value,Double.parseDouble(dis.txtYP.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.YAW_I.value,Double.parseDouble(dis.txtYI.getText())));
				comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_PID,PenguinPIDEnum.YAW_D.value,Double.parseDouble(dis.txtYD.getText())));
			}catch(Exception e){
				System.out.println("ERROR - Cannot parse PID values.");
			}
		}
	}
	
	public void on_1hz_timer(Event ev){
		
	}
	
	public void on_10hz_timer(Event ev){
		
	}
	
	public void on_20hz_timer(Event ev){
		if(throttle_up){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.INPUT_THRUST.value,30.0));
		}
		if(throttle_down){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.INPUT_THRUST.value,-30.0));
		}
		
		if(pitch_off_plus){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,10.0));
		}
		if(pitch_off_neg){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_PITCH.value,-10.0));
		}
		if(roll_off_plus){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,10.0));
		}
		if(roll_off_neg){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_ROLL.value,-10.0));
		}
		if(yaw_off_plus){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_YAW.value,10.0));
		}
		if(roll_off_neg){
			comm.sendEvent(new Event(EventEnum.ROBOT_EVENT_VARIABLE,PenguinControlEnum.OFF_YAW.value,-10.0));
		}
	}
	
	public void on_25hz_timer(Event ev){
		
	}
	
	public void on_50hz_timer(Event ev){
		
	}
	
	public void on_100hz_timer(Event ev){
		
	}
	
	public void on_heartbeat_timer(Event ev){
		if(heartbeat <= 5){
			dis.btnPenguinConnected.setBackground(Color.GREEN);
		}
		else{
			dis.btnPenguinConnected.setBackground(Color.RED);
		}
		
	}
	
	public void on_motor(Event ev){
		
	}
	
	public void on_solenoid(Event ev) {
		
	}
	
	public void on_pose(Event ev) {
		
	}
	
	public void on_adc(Event ev) {
		
	}

	public void on_variable(Event ev){
		
	}
	
	public void on_imu(Event ev){
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
	
	public void on_pid(Event ev){
		if(ev.getIndex()==PenguinPIDEnum.PID_UPDATE_SUCCESS.value){
			System.out.println("PID successfully updated."); 
		}
	}
	
	public void on_encoder(Event ev){
		
	}

	public void on_eeprom(Event ev){
		
	}

	public void on_io(Event ev){
		
	}
	
	public void on_shutdown(Event ev){
		
	}
	
	public void on_unknown_command(Event ev){
		
	}

}
