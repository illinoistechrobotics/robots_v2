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

void on_init(){
  initGEDC6();
  initPID();
}

void on_command_code(robot_event *ev){
  
}

void on_failsafe(robot_event *ev){
  
}

void on_status(robot_event *ev){
  
}

void on_axis_change(robot_event *ev){
  
}

void on_button_down(robot_event *ev){
  
}

void on_button_up(robot_event *ev){
  
}

void on_joy_hat(robot_event *ev){
  
}

void on_joy_status(robot_event *ev){
  
}

void on_keyboard(robot_event *ev){
  
}

void on_display(robot_event *ev){
  
}

void on_p1hz_timer(robot_event *ev){
  
}

void on_1hz_timer(robot_event *ev){
  
}

void on_2hz_timer(robot_event *ev){
  
}

void on_5hz_timer(robot_event *ev){
  
}

void on_10hz_timer(robot_event *ev){
  
}

void on_20hz_timer(robot_event *ev){
  robot_event new_ev;
  new_ev.command = ROBOT_EVENT_IMU;
  new_ev.type = FLOAT;
  
  new_ev.index = PITCH;
  new_ev.f = sensor.pitch;
  robot.sendEvent(&new_ev);
  
  new_ev.index = ROLL;
  new_ev.f = sensor.roll;
  robot.sendEvent(&new_ev);
  
  new_ev.index = YAW;
  new_ev.f = sensor.yaw;
  robot.sendEvent(&new_ev);
  
  new_ev.type = INTEGER;
  
  new_ev.index = MOT0;
  new_ev.i = motor_value[0];
  robot.sendEvent(&new_ev);
  
  new_ev.index = MOT1;
  new_ev.i = motor_value[1];
  robot.sendEvent(&new_ev);
  
  new_ev.index = MOT2;
  new_ev.i = motor_value[2];
  robot.sendEvent(&new_ev);
  
  new_ev.index = MOT3;
  new_ev.i = motor_value[3];
  robot.sendEvent(&new_ev);
  
}

void on_25hz_timer(robot_event *ev){

}

void on_50hz_timer(robot_event *ev){
  
}

void on_100hz_timer(robot_event *ev){
  
}

void on_other_timer(robot_event *ev){
  
}

void on_motor(robot_event *ev){
  
}

void on_relay(robot_event *ev){
  
}

void on_pose(robot_event *ev){
  
}

void on_adc(robot_event *ev){
  
}

void on_variable(robot_event *ev){
  if(ev->index == INPUT_THRUST){
    throttle_input = constrain(throttle_input + ev->f, 0, 1000);
  }
  else if(ev->index == INPUT_PITCH){
    angle_input.pitch = ev->f;
  }
  else if(ev->index == INPUT_ROLL){
    angle_input.roll = ev->f;
  }
  else if(ev->index == INPUT_YAW){
    angle_input.yaw = ev->f;
  }
  else if(ev->index == OFF_PITCH){
    angle_off.pitch = ev->f;
  }
  else if(ev->index == OFF_ROLL){
    angle_off.roll = ev->f;
  }
  else if(ev->index == OFF_ROLL){
    angle_off.yaw = ev->f;
  }
  else if(ev->index == OUTPUT_MOTORS){
    output_motors = ev->i;
  }
}

void on_imu(robot_event *ev){
  
}

void on_pid(robot_event *ev){
  static int count = 0;
  count++;
  if(ev->index == PITCH_P){
    pid_pitch.P = ev->f;
    count = 1;
  }
  else if(ev->index == PITCH_I){
    pid_pitch.I = ev->f;
  }
  else if(ev->index == PITCH_D){
    pid_pitch.D = ev->f;
  }
  else if(ev->index == ROLL_P){
    pid_roll.P = ev->f;
  }
  else if(ev->index == ROLL_I){
    pid_roll.I = ev->f;
  }
  else if(ev->index == ROLL_D){
    pid_roll.D = ev->f;
  }
  else if(ev->index == YAW_P){
    pid_yaw.P = ev->f;
  }
  else if(ev->index == YAW_I){
    pid_yaw.I = ev->f;
  }
  else if(ev->index == YAW_D){
    pid_yaw.D = ev->f;
    if(count==9){
      robot_event new_ev;
      new_ev.command = ROBOT_EVENT_PID;
      new_ev.index = PID_UPDATE_SUCCESS;
      new_ev.i = 0;
      new_ev.type = 0;
      robot.sendEvent(&new_ev);
    }
  }
}

void on_encoder(robot_event *ev){
  
}

void on_eeprom(robot_event *ev){
  
}

void on_io(robot_event *ev){
  
}

void on_shutdown(robot_event *ev){
  
}

void on_unknown_command(robot_event *ev){
  
}

void on_fast_loop(){
  readGEDC6();
}





