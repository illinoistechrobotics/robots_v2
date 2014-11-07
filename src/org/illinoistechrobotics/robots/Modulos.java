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
package org.illinoistechrobotics.robots;

import java.awt.Color;

import org.illinoistechrobotics.common.Communication;
import org.illinoistechrobotics.common.Event;
import org.illinoistechrobotics.common.GUI;
import org.illinoistechrobotics.common.Queue;
import org.illinoistechrobotics.common.Robot;
import org.illinoistechrobotics.common.Timer;

public class Modulos extends Robot{
	
	public Modulos(Queue q, Communication c, GUI d, Timer t){
		super(q,c,d,t);
	}
	
	public void on_init(){

	}
	
	public void on_command_code(Event ev){
		
	}
	
	public void on_heartbeat(Event ev) {
		
	}
	
	public void on_status(Event ev){
		
	}
	
	public void on_axis_change(Event ev){

	}

	public void on_button_down(Event ev){

	}
	
	public void on_button_up(Event ev){
		
	}
	
	public void on_joy_hat(Event ev){

	}
	
	public void on_joy_status(Event ev){
		
	}
	
	public void on_keyboard(Event ev){
		
	}
	
	public void on_display(Event ev){
		
	}
	
	public void on_gui(Event ev){

	}
	
	public void on_1hz_timer(Event ev){
		
	}
	
	public void on_10hz_timer(Event ev){
		
	}
	
	public void on_20hz_timer(Event ev){

	}
	
	public void on_25hz_timer(Event ev){
		
	}
	
	public void on_50hz_timer(Event ev){
		
	}
	
	public void on_100hz_timer(Event ev){
		
	}
	
	public void on_heartbeat_timer(Event ev){
		if(heartbeat <= 5){
			dis.btnModulosConnected.setBackground(Color.GREEN);
		}
		else{
			dis.btnModulosConnected.setBackground(Color.RED);
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
		
	}
	
	public void on_pid(Event ev){

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
