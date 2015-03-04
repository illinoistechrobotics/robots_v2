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
import org.bulldog.core.Signal;
import org.bulldog.core.gpio.DigitalOutput;
import org.bulldog.core.platform.Board;
import org.bulldog.core.platform.Platform;
import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.EventManager;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.RobotEnum;
import org.illinoistechrobotics.common.Timer;
import org.illinoistechrobotics.jaguar.Jaguar;

public class Modulus extends EventManager {

	private Board board;
	private DigitalOutput output;
	private Jaguar jaguar;

	public Modulus(Queue q, Communication c, Timer t) {
		super(q, c, t, RobotEnum.MODULUS);
	}

	@Override
	public void on_init() {
		super.on_init();
		board = Platform.createBoard();
		output = board.getPin(BBBNames.P8_12).as(DigitalOutput.class);
		output.write(Signal.High);
		timer.timer1hz = true;

		String port = System.getProperty("jaguar.port", "/dev/ttyO0");
		jaguar = new Jaguar();
		jaguar.open(port);
	}

	@Override
	public void on_failsafe() {

	}

	@Override
	public void on_axis_change(Event ev) {
		super.on_axis_change(ev);
		System.out.println(ev.toString());

	}

	@Override
	public void on_1hz_timer(Event ev) {
		super.on_1hz_timer(ev);
		output.toggle();
		System.out.println("1hz");
	}

}
