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

import org.bulldog.beagleboneblack.BBBNames;
import org.bulldog.core.gpio.DigitalOutput;
import org.bulldog.core.gpio.Pwm;
import org.bulldog.core.platform.Board;
import org.bulldog.core.platform.Platform;
import org.bulldog.devices.servo.Servo;
import org.bulldog.devices.servo.TowerProMicroSG90;
import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventManager;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.RobotEnum;
import org.illinoistechrobotics.common.Timer;
import org.illinoistechrobotics.jaguar.Jaguar;

public class Modulus extends EventManager {

	private Board board;
	private DigitalOutput[] spike = new DigitalOutput[4];
	private Servo[] talons = new Servo[4];
	private Jaguar jaguar;
	private boolean failsafe = true;

	public Modulus(Queue q, Communication c, Timer t) {
		super(q, c, t, RobotEnum.MODULUS);
	}

	@Override
	public void on_init() {
		super.on_init();
		board = Platform.createBoard();
		spike[0] = board.getPin(BBBNames.P9_26).as(DigitalOutput.class);
		spike[1] = board.getPin(BBBNames.P9_28).as(DigitalOutput.class);
		spike[2] = board.getPin(BBBNames.P9_30).as(DigitalOutput.class);
		spike[3] = board.getPin(BBBNames.P9_32).as(DigitalOutput.class);
		
		talons[0] = new TowerProMicroSG90(board.getPin(BBBNames.PWM_P9_14).as(Pwm.class));
		talons[1] = new TowerProMicroSG90(board.getPin(BBBNames.PWM_P9_16).as(Pwm.class));
		talons[2] = new TowerProMicroSG90(board.getPin(BBBNames.PWM_P8_13).as(Pwm.class));
		talons[3] = new TowerProMicroSG90(board.getPin(BBBNames.PWM_P8_19).as(Pwm.class));
				
		timer.timer1hz = true;
		timer.timer10hz = true;

		String port = System.getProperty("jaguar.port", "/dev/ttyO0");
		jaguar = new Jaguar();
		jaguar.open(port);
		for(int i=1; i<5; i++){
			jaguar.configEncoderLines(i, 500);
			jaguar.positionPID(i, 0x4e200000, 0x00000000, 0x00000000);
			jaguar.positionEnable(i, 0);
		}
	}

	@Override
	public void on_failsafe() {
		talons[0].setAngle(180);
		talons[1].setAngle(180);
		talons[2].setAngle(180);
		talons[3].setAngle(180);
		
		spike[0].low();
		spike[1].low();
		spike[2].low();
		spike[3].low();
	}

	@Override
	public void on_motor(Event ev) {
		int id = ev.getIndex();
		int pos = ev.getValue();
		if(id >= 1 && id <= 4){
			jaguar.positionSet(id, pos);
		}
		if(id >= 5 && id <= 8){
			id = id - 5;
			talons[id].setAngle(pos);
		}
		
	}

	@Override
	public void on_solenoid(Event ev) {
		int id = ev.getIndex();
		int pos = ev.getValue();
		
		if(pos > 0){
			spike[id].high();
		}
		else {
			spike[id].low();
		}
	}
	
	@Override
	public void on_1hz_timer(Event ev) {
		super.on_1hz_timer(ev);
		System.out.println("1hz");
	}
	
	@Override
	public void on_10hz_timer(Event ev) {
		if(!failsafe){
			jaguar.sysHeartBeat(1);
			jaguar.sysHeartBeat(2);
			jaguar.sysHeartBeat(3);
			jaguar.sysHeartBeat(4);
		}
	}
	

}
